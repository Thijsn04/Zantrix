import React, { useEffect, useState } from 'react';
import { useEditor, EditorContent } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import Placeholder from '@tiptap/extension-placeholder';
import { SmartTemplate } from '../../api/charting.api';
import { SmartPhrasePopover } from './SmartPhrasePopover';

interface RichClinicalEditorProps {
  initialContent?: string;
  isReadOnly?: boolean;
  templates?: SmartTemplate[];
  onChange?: (json: string) => void;
}

export const RichClinicalEditor: React.FC<RichClinicalEditorProps> = ({
  initialContent,
  isReadOnly = false,
  templates = [],
  onChange
}) => {
  const [popoverOpen, setPopoverOpen] = useState(false);
  const [popoverPos, setPopoverPos] = useState({ top: 0, left: 0 });
  const [filterText, setFilterText] = useState('');
  
  const editor = useEditor({
    extensions: [
      StarterKit,
      Placeholder.configure({
        placeholder: 'Begin met typen of gebruik . voor een smart phrase...',
      }),
    ],
    content: initialContent ? JSON.parse(initialContent) : '',
    editable: !isReadOnly,
    onUpdate: ({ editor }) => {
      if (onChange) {
        onChange(JSON.stringify(editor.getJSON()));
      }
      
      // Smart Phrase detection
      const textBeforeCursor = editor.state.doc.textBetween(
        Math.max(0, editor.state.selection.from - 10),
        editor.state.selection.from,
        ' '
      );
      
      const match = textBeforeCursor.match(/\.(\w*)$/);
      if (match) {
        const { view } = editor;
        const coords = view.coordsAtPos(editor.state.selection.from);
        setPopoverPos({ top: coords.top + window.scrollY + 20, left: coords.left + window.scrollX });
        setFilterText(match[1]);
        setPopoverOpen(true);
      } else {
        setPopoverOpen(false);
      }
    },
  });

  const handleSelectTemplate = (template: SmartTemplate) => {
    if (!editor) return;
    
    // Calculate position to delete the `.phrase`
    const { from } = editor.state.selection;
    const dotPos = from - filterText.length - 1;
    
    editor
      .chain()
      .focus()
      .deleteRange({ from: dotPos, to: from })
      .insertContent(template.contentTemplate)
      .run();
      
    setPopoverOpen(false);
  };

  useEffect(() => {
    if (editor && initialContent) {
      // Just to sync if initial content changes completely (e.g., loading different note)
      if (editor.getJSON() !== JSON.parse(initialContent)) {
        editor.commands.setContent(JSON.parse(initialContent));
      }
    }
  }, [editor, initialContent]);

  return (
    <div className={`prose max-w-none border rounded-md p-4 bg-white ${isReadOnly ? 'opacity-70 cursor-not-allowed' : ''}`}>
      <EditorContent editor={editor} />
      
      {popoverOpen && (
        <SmartPhrasePopover 
          position={popoverPos} 
          filter={filterText}
          templates={templates}
          onSelect={handleSelectTemplate}
          onClose={() => setPopoverOpen(false)}
        />
      )}
    </div>
  );
};
