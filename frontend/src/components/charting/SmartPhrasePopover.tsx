import React from 'react';
import { SmartTemplate } from '../../api/charting.api';

interface SmartPhrasePopoverProps {
  position: { top: number; left: number };
  filter: string;
  templates: SmartTemplate[];
  onSelect: (template: SmartTemplate) => void;
  onClose: () => void;
}

export const SmartPhrasePopover: React.FC<SmartPhrasePopoverProps> = ({
  position,
  filter,
  templates,
  onSelect
}) => {
  const filteredTemplates = templates.filter(t => 
    t.shortcut.toLowerCase().includes(`.${filter.toLowerCase()}`) || 
    t.title.toLowerCase().includes(filter.toLowerCase())
  );

  if (filteredTemplates.length === 0) return null;

  return (
    <div 
      className="absolute z-50 bg-white border border-slate-200 shadow-lg rounded-md w-64 max-h-64 overflow-y-auto"
      style={{ top: position.top, left: position.left }}
    >
      <div className="p-2 bg-slate-50 text-xs font-semibold text-slate-500 border-b">
        Smart Phrases
      </div>
      <ul className="py-1">
        {filteredTemplates.map(template => (
          <li 
            key={template.id || template.shortcut}
            className="px-3 py-2 hover:bg-slate-100 cursor-pointer flex flex-col"
            onClick={() => onSelect(template)}
          >
            <span className="font-medium text-slate-800">{template.shortcut}</span>
            <span className="text-xs text-slate-500 truncate">{template.title}</span>
          </li>
        ))}
      </ul>
    </div>
  );
};
