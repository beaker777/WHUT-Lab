import type { CellData } from "../types/game";

const EMPTY_CELL_TYPE = 0;

function createCell(row: number, col: number, type: number): CellData {
  return {
    row,
    col,
    type,
    isEmpty: type === EMPTY_CELL_TYPE,
    isSelected: false
  };
}

export function createBoardFromMap(map: number[][]): CellData[][] {
  return map.map((rowValues, rowIndex) =>
    rowValues.map((value, colIndex) => createCell(rowIndex, colIndex, Math.max(value, EMPTY_CELL_TYPE)))
  );
}

export function createEmptyBoard(rows = 0, cols = 0): CellData[][] {
  return Array.from({ length: rows }, (_, rowIndex) =>
    Array.from({ length: cols }, (_, colIndex) => createCell(rowIndex, colIndex, EMPTY_CELL_TYPE))
  );
}

export function createMapFromBoard(board: CellData[][]): number[][] {
  return board.map((row) => row.map((cell) => (cell.isEmpty ? EMPTY_CELL_TYPE : cell.type)));
}

export function updateBoardCell(
  board: CellData[][],
  target: Pick<CellData, "row" | "col">,
  updates: Partial<CellData>
): CellData[][] {
  return board.map((rowCells, rowIndex) =>
    rowCells.map((cell, colIndex) =>
      rowIndex === target.row && colIndex === target.col ? { ...cell, ...updates } : cell
    )
  );
}
