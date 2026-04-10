import { memo, useCallback } from "react";
import {
  Cloud,
  Gem,
  Heart,
  Leaf,
  Moon,
  Star,
  Sun,
  Zap
} from "lucide-react";
import type { CellData } from "../types/game";

const TILE_ICONS = [Heart, Star, Sun, Moon, Cloud, Leaf, Gem, Zap] as const;
const TILE_COLORS = [
  "text-rose-400",
  "text-amber-400",
  "text-orange-400",
  "text-violet-400",
  "text-sky-400",
  "text-emerald-400",
  "text-cyan-500",
  "text-fuchsia-400"
] as const;

interface GameTileProps {
  cell: CellData;
  cellKey: string;
  isHinted: boolean;
  isInteractionDisabled: boolean;
  onCellClick: (cell: CellData) => void;
  setCellRef: (cellKey: string, element: HTMLButtonElement | null) => void;
}

function buildCellButtonClassName(cell: CellData, isHinted: boolean) {
  return [
    "relative z-10 aspect-square rounded-2xl border text-xl transition duration-200 md:text-2xl",
    "backdrop-blur-xl focus:outline-none focus:ring-2 focus:ring-pink-200/90",
    cell.isEmpty
      ? "border-white/10 bg-white/10 text-transparent"
      : "border-white/90 bg-white/95 text-slate-800 shadow-[0_4px_12px_rgba(0,0,0,0.05)] hover:scale-[1.05] hover:border-pink-300 hover:bg-white hover:shadow-[0_8px_20px_rgba(244,114,182,0.2)]",
    cell.isSelected ? "shadow-glow ring-2 ring-pink-300" : "",
    !cell.isSelected && isHinted
      ? "hint-tile-blink border-amber-200 bg-gradient-to-br from-rose-50 via-amber-50 to-sky-50 ring-2 ring-amber-200/80"
      : ""
  ].join(" ");
}

function GameTileComponent({
  cell,
  cellKey,
  isHinted,
  isInteractionDisabled,
  onCellClick,
  setCellRef
}: GameTileProps) {
  const IconComponent = TILE_ICONS[(Math.max(cell.type, 1) - 1) % TILE_ICONS.length];
  const iconColorClass = TILE_COLORS[(Math.max(cell.type, 1) - 1) % TILE_COLORS.length];
  const handleClick = useCallback(() => {
    onCellClick(cell);
  }, [cell, onCellClick]);
  const handleRef = useCallback((element: HTMLButtonElement | null) => {
    setCellRef(cellKey, element);
  }, [cellKey, setCellRef]);

  return (
    <button
      ref={handleRef}
      type="button"
      onClick={handleClick}
      disabled={isInteractionDisabled}
      className={buildCellButtonClassName(cell, isHinted)}
      aria-label={`第${cell.row + 1}行 第${cell.col + 1}列`}
    >
      {!cell.isEmpty && (
        <IconComponent
          className={`mx-auto h-6 w-6 drop-shadow-[0_4px_18px_rgba(255,181,208,0.35)] md:h-7 md:w-7 ${iconColorClass}`}
          strokeWidth={2.25}
        />
      )}
    </button>
  );
}

export const GameTile = memo(GameTileComponent, (previousProps, nextProps) => {
  return previousProps.cell === nextProps.cell
    && previousProps.isHinted === nextProps.isHinted
    && previousProps.isInteractionDisabled === nextProps.isInteractionDisabled;
});
