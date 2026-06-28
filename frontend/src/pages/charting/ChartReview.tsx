import React, { useState, useEffect } from 'react';
import { ClinicalNote, SmartTemplate, fetchNotes, saveDraft, signNote, fetchTemplates } from '../../api/charting.api';
import { RichClinicalEditor } from '../../components/charting/RichClinicalEditor';
import { useParams } from 'react-router-dom';
import { FileSignature, Plus, Save, AlertTriangle } from 'lucide-react';

export const ChartReview: React.FC = () => {
  const { patientId } = useParams<{ patientId: string }>();
  const [notes, setNotes] = useState<ClinicalNote[]>([]);
  const [templates, setTemplates] = useState<SmartTemplate[]>([]);
  const [activeNote, setActiveNote] = useState<ClinicalNote | null>(null);
  const [isSaving, setIsSaving] = useState(false);

  // MOCK author ID for MVP purposes
  const CURRENT_AUTHOR_ID = '00000000-0000-0000-0000-000000000001';

  useEffect(() => {
    if (patientId) {
      loadNotes();
      loadTemplates();
    }
  }, [patientId]);

  const loadNotes = async () => {
    if (!patientId) return;
    try {
      const data = await fetchNotes(patientId);
      setNotes(data);
    } catch (err) {
      console.error("Failed to load notes", err);
    }
  };

  const loadTemplates = async () => {
    try {
      // Fetching global + personal templates
      const data = await fetchTemplates(CURRENT_AUTHOR_ID);
      setTemplates(data);
    } catch (err) {
      console.error("Failed to load templates", err);
    }
  };

  const handleNewNote = () => {
    if (!patientId) return;
    setActiveNote({
      patientId,
      authorId: CURRENT_AUTHOR_ID,
      noteType: 'Consultation Note',
      content: JSON.stringify({
        type: 'doc',
        content: [{ type: 'paragraph' }]
      })
    });
  };

  const handleEditorChange = (jsonStr: string) => {
    if (activeNote) {
      setActiveNote({ ...activeNote, content: jsonStr });
    }
  };

  const handleSaveDraft = async () => {
    if (!activeNote) return;
    setIsSaving(true);
    try {
      const saved = await saveDraft(activeNote);
      setActiveNote(saved);
      loadNotes();
    } catch (err) {
      console.error(err);
    } finally {
      setIsSaving(false);
    }
  };

  const handleSign = async () => {
    if (!activeNote || !activeNote.id) {
      // If it's brand new, save draft first
      await handleSaveDraft();
    }
    
    // activeNote.id should exist now if the above completed, or if it was already saved
    const noteIdToSign = activeNote?.id;
    if (!noteIdToSign) return;

    try {
      setIsSaving(true);
      const signed = await signNote(noteIdToSign, CURRENT_AUTHOR_ID);
      setActiveNote(signed);
      loadNotes();
    } catch (err) {
      console.error("Signature failed", err);
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="flex h-full bg-slate-50 overflow-hidden">
      {/* Sidebar: Note List */}
      <div className="w-1/3 border-r bg-white overflow-y-auto flex flex-col">
        <div className="p-4 border-b flex justify-between items-center bg-slate-100">
          <h2 className="font-semibold text-slate-800">Chart Review</h2>
          <button 
            onClick={handleNewNote}
            className="flex items-center gap-1 bg-blue-600 text-white px-3 py-1.5 rounded-md text-sm hover:bg-blue-700"
          >
            <Plus size={16} /> New Note
          </button>
        </div>
        
        <div className="flex-1 overflow-y-auto">
          {notes.length === 0 && (
            <div className="p-8 text-center text-slate-400">No notes found for this patient.</div>
          )}
          {notes.map(note => (
            <div 
              key={note.id} 
              onClick={() => setActiveNote(note)}
              className={`p-4 border-b cursor-pointer hover:bg-blue-50 transition-colors ${activeNote?.id === note.id ? 'bg-blue-50 border-l-4 border-l-blue-600' : ''}`}
            >
              <div className="flex justify-between items-start mb-1">
                <span className="font-medium text-slate-900">{note.noteType}</span>
                <span className={`text-xs px-2 py-0.5 rounded-full ${
                  note.status === 'FINAL' ? 'bg-green-100 text-green-800' : 
                  note.status === 'IN_PROGRESS' ? 'bg-yellow-100 text-yellow-800' : 
                  'bg-slate-100 text-slate-800'
                }`}>
                  {note.status}
                </span>
              </div>
              <div className="text-xs text-slate-500">
                {note.createdAt ? new Date(note.createdAt).toLocaleString() : 'Just now'}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Main Content: Editor */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {activeNote ? (
          <>
            <div className="p-4 border-b bg-white flex justify-between items-center">
              <div>
                <h3 className="font-semibold text-lg">{activeNote.noteType}</h3>
                <p className="text-xs text-slate-500">
                  {activeNote.status === 'FINAL' ? `Signed at ${new Date(activeNote.signedAt!).toLocaleString()}` : 'Draft'}
                </p>
              </div>
              
              {activeNote.status !== 'FINAL' && activeNote.status !== 'AMENDED' && (
                <div className="flex gap-2">
                  <button 
                    onClick={handleSaveDraft}
                    disabled={isSaving}
                    className="flex items-center gap-2 px-4 py-2 border rounded-md text-slate-600 hover:bg-slate-50"
                  >
                    <Save size={16} /> {isSaving ? 'Saving...' : 'Save Draft'}
                  </button>
                  <button 
                    onClick={handleSign}
                    disabled={isSaving}
                    className="flex items-center gap-2 px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700"
                  >
                    <FileSignature size={16} /> Sign Note
                  </button>
                </div>
              )}
            </div>
            
            <div className="flex-1 p-6 overflow-y-auto bg-slate-50">
              {activeNote.status === 'FINAL' && (
                <div className="mb-4 p-3 bg-yellow-50 border border-yellow-200 text-yellow-800 rounded-md flex items-start gap-3">
                  <AlertTriangle className="mt-0.5" size={18} />
                  <div>
                    <strong className="block text-sm">Note is Signed</strong>
                    <span className="text-xs">This clinical document is legally locked and cannot be modified.</span>
                  </div>
                </div>
              )}
              
              <RichClinicalEditor 
                initialContent={activeNote.content}
                isReadOnly={activeNote.status === 'FINAL' || activeNote.status === 'AMENDED'}
                templates={templates}
                onChange={handleEditorChange}
              />
            </div>
          </>
        ) : (
          <div className="flex-1 flex items-center justify-center text-slate-400">
            Select a note to view or create a new one.
          </div>
        )}
      </div>
    </div>
  );
};
