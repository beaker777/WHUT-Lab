import type { CellData } from "../types/game";

export const ROWS = 10;
export const COLS = 16;

export function createBoardFromMap(map: number[][]): CellData[][] {
  return map.map((row, x) =>
    row.map((value, y) => ({
      x,
      y,
      type: value > 0 ? value : 0,
      isEmpty: value === 0,
      isSelected: false
    }))
  );
}

export function createEmptyBoard(): CellData[][] {
  return Array.from({ length: ROWS }, (_, x) =>
    Array.from({ length: COLS }, (_, y) => ({
      x,
      y,
      type: 0,
      isEmpty: true,
      isSelected: false
    }))
  );
}

export function createMapFromBoard(board: CellData[][]): number[][] {
  return board.map((row) => row.map((cell) => (cell.isEmpty ? 0 : cell.type)));
}

export function updateBoardCell(
  board: CellData[][],
  target: Pick<CellData, "x" | "y">,
  updates: Partial<CellData>
): CellData[][] {
  return board.map((row, rowIndex) =>
    row.map((cell, colIndex) =>
      rowIndex === target.x && colIndex === target.y ? { ...cell, ...updates } : cell
    )
  );
}
