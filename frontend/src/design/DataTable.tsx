import type { ReactNode } from 'react';
import { EmptyState } from './Panel';

export interface Column<T> {
  /** Column heading. */
  header: string;
  /** Cell content for one row. */
  cell: (row: T) => ReactNode;
  /** Optional narrow/numeric presentation hint. */
  align?: 'start' | 'end';
}

/**
 * A real HTML table. Clinical data is tabular, so it uses native table
 * semantics rather than ARIA roles layered onto divs: screen readers then get
 * row and column relationships for free.
 */
export function DataTable<T>({ caption, columns, rows, rowKey, empty }: {
  caption: string;
  columns: Column<T>[];
  rows: T[];
  rowKey: (row: T) => string;
  empty: string;
}) {
  if (!rows.length) return <EmptyState>{empty}</EmptyState>;
  return (
    <div className="table-scroll">
      <table className="data-table">
        <caption className="visually-hidden">{caption}</caption>
        <thead>
          <tr>{columns.map(column =>
            <th key={column.header} scope="col" className={column.align === 'end' ? 'align-end' : undefined}>{column.header}</th>)}
          </tr>
        </thead>
        <tbody>
          {rows.map(row =>
            <tr key={rowKey(row)}>{columns.map(column =>
              <td key={column.header} className={column.align === 'end' ? 'align-end' : undefined}>{column.cell(row)}</td>)}
            </tr>)}
        </tbody>
      </table>
    </div>
  );
}
