import { useCallback, useEffect, useMemo, useRef, useState, type CSSProperties } from "react";
import {
  Lightbulb,
} from "lucide-react";
import { fetchHint, initGameBoard, matchCheck } from "../services/gameApi";
import type { CellData, DifficultyKey, GameRuleConfig, MatchVertex } from "../types/game";
import { createBoardFromMap, createEmptyBoard, createMapFromBoard, updateBoardCell } from "../utils/board";
import { GameTile } from "./GameTile";

const LINE_DISPLAY_MS = 650;
const HINT_DISPLAY_MS = 2400;

const DIFFICULTY_LABELS: Record<DifficultyKey, string> = {
  EASY: "简单",
  NORMAL: "普通",
  HARD: "困难"
};

interface LinePoint {
  x: number;
  y: number;
}

interface HappyLinkGameProps {
  initialDifficulty: DifficultyKey;
  elapsedSeconds: number;
  isPaused: boolean;
  onGameWon: () => void;
  onPauseRequest: () => void;
}

function formatElapsedTime(totalSeconds: number) {
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  return `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;
}

export function HappyLinkGame({
  initialDifficulty,
  elapsedSeconds,
  isPaused,
  onGameWon,
  onPauseRequest
}: HappyLinkGameProps) {
  const [board, setBoard] = useState<CellData[][]>(() => createEmptyBoard());
  const [ruleConfig, setRuleConfig] = useState<GameRuleConfig | null>(null);
  const [selectedCells, setSelectedCells] = useState<CellData[]>([]);
  const [statusMessage, setStatusMessage] = useState("棋盘准备中，马上开始配对。");
  const [deadlockNotice, setDeadlockNotice] = useState("");
  const [hintCells, setHintCells] = useState<MatchVertex[]>([]);
  const [pathPreview, setPathPreview] = useState<MatchVertex[]>([]);
  const [linePoints, setLinePoints] = useState<LinePoint[]>([]);
  const [isPathFading, setIsPathFading] = useState(false);
  const [isChecking, setIsChecking] = useState(false);
  const [isLoadingBoard, setIsLoadingBoard] = useState(false);
  const [hasBoardLoaded, setHasBoardLoaded] = useState(false);
  const boardRef = useRef<HTMLDivElement | null>(null);
  const cellRefs = useRef<Record<string, HTMLButtonElement | null>>({});
  const clearLineTimerRef = useRef<number | null>(null);
  const removeLineTimerRef = useRef<number | null>(null);
  const clearHintTimerRef = useRef<number | null>(null);
  const hintFrameRef = useRef<number | null>(null);
  const hasReportedVictoryRef = useRef(false);
  const selectedCellsRef = useRef<CellData[]>([]);

  const rowCount = ruleConfig?.rows ?? board.length;
  const colCount = ruleConfig?.cols ?? board[0]?.length ?? 0;
  const selectedCount = selectedCells.length;
  const totalCells = useMemo(() => rowCount * colCount, [rowCount, colCount]);
  const remainingTileCount = useMemo(
    () => board.flat().filter((cell) => !cell.isEmpty).length,
    [board]
  );
  const formattedElapsedTime = useMemo(
    () => formatElapsedTime(elapsedSeconds),
    [elapsedSeconds]
  );
  const linePath = useMemo(
    () => linePoints.map((point) => `${point.x},${point.y}`).join(" "),
    [linePoints]
  );
  const lineLength = useMemo(() => {
    if (linePoints.length < 2) {
      return 0;
    }

    return linePoints.slice(1).reduce((totalLength, point, index) => {
      const previousPoint = linePoints[index];
      return totalLength + Math.hypot(point.x - previousPoint.x, point.y - previousPoint.y);
    }, 0);
  }, [linePoints]);
  const gradientStartPoint = linePoints[0];
  const gradientEndPoint = linePoints[linePoints.length - 1];
  const difficultyLabel = ruleConfig ? DIFFICULTY_LABELS[ruleConfig.difficulty] : DIFFICULTY_LABELS[initialDifficulty];

  const clearLineTimers = () => {
    if (clearLineTimerRef.current !== null) {
      window.clearTimeout(clearLineTimerRef.current);
      clearLineTimerRef.current = null;
    }
    if (removeLineTimerRef.current !== null) {
      window.clearTimeout(removeLineTimerRef.current);
      removeLineTimerRef.current = null;
    }
  };

  const clearHintTimer = () => {
    if (clearHintTimerRef.current !== null) {
      window.clearTimeout(clearHintTimerRef.current);
      clearHintTimerRef.current = null;
    }
    if (hintFrameRef.current !== null) {
      window.cancelAnimationFrame(hintFrameRef.current);
      hintFrameRef.current = null;
    }
  };

  const hideLineImmediately = () => {
    clearLineTimers();
    setIsPathFading(false);
    setLinePoints([]);
  };

  const resetVisualState = () => {
    setSelectedCells([]);
    selectedCellsRef.current = [];
    setPathPreview([]);
    setDeadlockNotice("");
    clearHintTimer();
    setHintCells([]);
    hideLineImmediately();
  };

  const getCellKey = (row: number, col: number) => `${row}-${col}`;

  const buildMatchVertex = (cell: CellData) => ({
    row: cell.row,
    col: cell.col,
    type: cell.type
  });

  const hintCellKeys = useMemo(() => {
    return new Set(hintCells.map((hintCell) => getCellKey(hintCell.row, hintCell.col)));
  }, [hintCells]);
  const isInteractionDisabled = isPaused || isLoadingBoard || isChecking;
  const setCellRef = useCallback((cellKey: string, element: HTMLButtonElement | null) => {
    cellRefs.current[cellKey] = element;
  }, []);

  const clearSelectedState = (cells: CellData[]) => {
    setBoard((currentBoard) =>
      cells.reduce(
        (nextBoard, cell) => updateBoardCell(nextBoard, cell, { isSelected: false }),
        currentBoard
      )
    );
    setSelectedCells([]);
    selectedCellsRef.current = [];
  };

  const showHintTiles = (nextHintTiles: MatchVertex[]) => {
    clearHintTimer();
    setHintCells([]);

    hintFrameRef.current = window.requestAnimationFrame(() => {
      setHintCells(nextHintTiles);
      hintFrameRef.current = null;
      clearHintTimerRef.current = window.setTimeout(() => {
        setHintCells([]);
        clearHintTimerRef.current = null;
      }, HINT_DISPLAY_MS);
    });
  };

  const drawLinkLine = (path: MatchVertex[]) => {
    if (!boardRef.current || path.length < 2) {
      hideLineImmediately();
      return;
    }

    const boardRect = boardRef.current.getBoundingClientRect();
    const points = path
      .map((point) => {
        const cellElement = cellRefs.current[getCellKey(point.row, point.col)];
        if (!cellElement) {
          return null;
        }

        const cellRect = cellElement.getBoundingClientRect();
        return {
          x: cellRect.left - boardRect.left + cellRect.width / 2,
          y: cellRect.top - boardRect.top + cellRect.height / 2
        };
      })
      .filter((point): point is LinePoint => point !== null);

    clearLineTimers();
    setIsPathFading(false);
    setLinePoints(points);

    if (points.length > 1) {
      clearLineTimerRef.current = window.setTimeout(() => {
        setIsPathFading(true);
        clearLineTimerRef.current = null;
      }, LINE_DISPLAY_MS);

      removeLineTimerRef.current = window.setTimeout(() => {
        setLinePoints([]);
        setIsPathFading(false);
        removeLineTimerRef.current = null;
      }, LINE_DISPLAY_MS + 300);
    }
  };

  const loadInitialBoard = async () => {
    setIsLoadingBoard(true);

    try {
      const result = await initGameBoard(initialDifficulty);
      setBoard(createBoardFromMap(result.map));
      setRuleConfig(result.config);
      resetVisualState();
      setHasBoardLoaded(true);
      hasReportedVictoryRef.current = false;
      setStatusMessage(
        result.success
          ? `${DIFFICULTY_LABELS[result.config.difficulty]}模式已就绪，当前棋盘为 ${result.config.rows} x ${result.config.cols}，最多允许 ${result.config.maxTurns} 次拐弯。`
          : "棋盘初始化失败。"
      );
    } catch (error) {
      console.error(error);
      setStatusMessage("连接服务器失败，暂时无法加载棋盘。");
    } finally {
      setIsLoadingBoard(false);
    }
  };

  useEffect(() => {
    void loadInitialBoard();
    return () => {
      clearLineTimers();
      clearHintTimer();
    };
  }, [initialDifficulty]);

  useEffect(() => {
    if (!hasBoardLoaded || isLoadingBoard || remainingTileCount !== 0 || hasReportedVictoryRef.current) {
      return;
    }

    hasReportedVictoryRef.current = true;
    setStatusMessage("你已经清空整个棋盘，胜利属于你。");
    onGameWon();
  }, [hasBoardLoaded, isLoadingBoard, onGameWon, remainingTileCount]);

  useEffect(() => {
    selectedCellsRef.current = selectedCells;
  }, [selectedCells]);

  const handleMatchCheck = async (cell1: CellData, cell2: CellData) => {
    if (!ruleConfig) {
      clearSelectedState([cell1, cell2]);
      return;
    }

    setIsChecking(true);

    try {
      const result = await matchCheck({
        map: createMapFromBoard(board),
        config: ruleConfig,
        v1: buildMatchVertex(cell1),
        v2: buildMatchVertex(cell2)
      });
      setPathPreview(result.path ?? []);
      clearHintTimer();
      setHintCells([]);
      setStatusMessage(
        result.message
          || (result.connected ? "消除成功，继续寻找下一对图案。" : "这两个图案暂时无法连接，换一组试试。")
      );
      setDeadlockNotice(
        result.message.includes("已自动打乱")
          ? "当前棋盘已经没有可消除的组合，系统已为你自动重排剩余图块。"
          : ""
      );
      if (result.config) {
        setRuleConfig(result.config);
      }
      drawLinkLine(result.path ?? []);

      if (Array.isArray(result.map) && result.map.length > 0) {
        setBoard(createBoardFromMap(result.map));
        setSelectedCells([]);
        return;
      }

      clearSelectedState([cell1, cell2]);
    } catch (error) {
      console.error(error);
      setStatusMessage("匹配请求失败，请确认游戏服务运行正常。");
      clearSelectedState([cell1, cell2]);
    } finally {
      setIsChecking(false);
    }
  };

  const handleHintRequest = async () => {
    if (isPaused || isLoadingBoard || isChecking || !ruleConfig) {
      return;
    }

    clearHintTimer();
    setHintCells([]);
    if (selectedCells.length > 0) {
      clearSelectedState(selectedCells);
    }

    try {
      const result = await fetchHint(createMapFromBoard(board), ruleConfig);
      setStatusMessage(result.message || "已为你找到一组可消除图块。");
      if (result.config) {
        setRuleConfig(result.config);
      }

      if (!result.success || result.hintTiles.length !== 2) {
        return;
      }

      setDeadlockNotice("");
      showHintTiles(result.hintTiles);
    } catch (error) {
      console.error(error);
      setStatusMessage("提示请求失败，请确认游戏服务运行正常。");
    }
  };

  const handleCellClick = useCallback((cell: CellData) => {
    const currentSelectedCells = selectedCellsRef.current;

    if (isPaused || isLoadingBoard || isChecking || cell.isEmpty || cell.isSelected || currentSelectedCells.length >= 2) {
      return;
    }

    clearHintTimer();
    setHintCells([]);

    const nextSelectedCells = [...currentSelectedCells, { ...cell, isSelected: true }];
    setBoard((currentBoard) => updateBoardCell(currentBoard, cell, { isSelected: true }));
    setSelectedCells(nextSelectedCells);
    selectedCellsRef.current = nextSelectedCells;

    if (nextSelectedCells.length === 2) {
      void handleMatchCheck(nextSelectedCells[0], nextSelectedCells[1]);
    }
  }, [isPaused, isLoadingBoard, isChecking, handleMatchCheck]);

  return (
    <main className="relative min-h-screen overflow-hidden bg-gradient-to-br from-pink-100 via-amber-50 to-sky-100 px-4 py-8 text-slate-800">
      <div className="pointer-events-none absolute inset-0 overflow-hidden">
        <div className="absolute left-[-4rem] top-12 h-40 w-40 rounded-full bg-pink-200/60 blur-3xl md:h-56 md:w-56" />
        <div className="absolute right-[8%] top-24 h-44 w-44 rounded-full bg-teal-100/60 blur-3xl md:h-64 md:w-64" />
        <div className="absolute bottom-16 left-[12%] h-48 w-48 rounded-full bg-sky-200/60 blur-3xl md:h-72 md:w-72" />
      </div>
      <div className="relative z-10 mx-auto flex min-h-[calc(100vh-4rem)] w-full max-w-[1440px] flex-col gap-8 rounded-[32px] border border-white/60 bg-white/50 p-6 shadow-[0_8px_32px_rgba(255,192,203,0.15)] backdrop-blur-xl lg:p-10">
        <header className="flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
          <div className="space-y-3">
            <p className="text-sm uppercase tracking-[0.4em] text-rose-400">
              Macaron Arcade
            </p>
            <div>
              <h1 className="text-4xl font-black tracking-[0.18em] text-slate-900 md:text-5xl">
                欢乐连连看
              </h1>
              <p className="mt-3 max-w-2xl text-sm leading-6 text-slate-700 md:text-base">
                当前为 {difficultyLabel} 模式，棋盘大小 {rowCount} x {colCount}，最多允许 {ruleConfig?.maxTurns ?? 0} 次拐弯。
              </p>
            </div>
          </div>

          <div className="grid gap-4 sm:grid-cols-[minmax(0,1fr)_auto] sm:items-center">
            <div className="rounded-2xl border border-white/65 bg-white/60 px-4 py-3 text-sm text-slate-700 backdrop-blur-xl">
              已选中 <span className="font-bold text-rose-400">{selectedCount}</span> / 2
              <span className="mx-2 text-slate-400">|</span>
              总格子 <span className="font-bold text-sky-400">{totalCells}</span>
              <span className="mx-2 text-slate-400">|</span>
              用时 <span className="font-bold text-amber-400">{formattedElapsedTime}</span>
            </div>
            <div className="flex flex-wrap gap-3">
              <button
                type="button"
                onClick={() => void handleHintRequest()}
                disabled={isLoadingBoard || isChecking || !ruleConfig}
                className="rounded-2xl border border-white/80 bg-gradient-to-r from-amber-100 via-rose-100 to-sky-100 px-5 py-4 text-base font-bold text-slate-800 shadow-[0_10px_30px_rgba(255,239,176,0.35)] transition duration-200 hover:scale-[1.02] hover:shadow-[0_16px_38px_rgba(251,191,36,0.2)] focus:outline-none focus:ring-4 focus:ring-amber-100 disabled:cursor-not-allowed disabled:opacity-70"
              >
                <span className="inline-flex items-center gap-2">
                  <Lightbulb className="h-4 w-4 text-amber-500" />
                  提示一下
                </span>
              </button>
              <button
                type="button"
                onClick={onPauseRequest}
                disabled={isLoadingBoard || isChecking}
                className="rounded-2xl border border-white/80 bg-white/85 px-5 py-4 text-base font-bold text-slate-700 shadow-[0_10px_30px_rgba(255,255,255,0.3)] transition duration-200 hover:scale-[1.02] hover:bg-white focus:outline-none focus:ring-4 focus:ring-sky-100 disabled:cursor-not-allowed disabled:opacity-70"
              >
                暂停游戏
              </button>
              <button
                type="button"
                onClick={() => void loadInitialBoard()}
                disabled={isLoadingBoard}
                className="rounded-2xl border border-white/80 bg-gradient-to-r from-pink-200 via-amber-100 to-green-100 px-6 py-4 text-base font-bold text-slate-900 shadow-[0_12px_40px_rgba(248,200,220,0.38)] transition duration-200 hover:scale-[1.02] hover:shadow-[0_18px_50px_rgba(205,231,255,0.4)] focus:outline-none focus:ring-4 focus:ring-pink-200/80 disabled:cursor-not-allowed disabled:opacity-70"
              >
                {isLoadingBoard ? "正在准备棋盘..." : "重新生成本难度棋盘"}
              </button>
            </div>
          </div>
        </header>

        <section className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_320px]">
          <div className="overflow-auto rounded-[28px] border border-white/65 bg-white/42 p-4 backdrop-blur-xl md:p-5">
            <div
              ref={boardRef}
              className="relative grid min-w-max gap-2 md:gap-2.5"
              data-board-grid="true"
              style={{
                gridTemplateRows: `repeat(${rowCount}, minmax(0, 1fr))`,
                gridTemplateColumns: `repeat(${colCount}, minmax(52px, 1fr))`
              }}
            >
              {linePoints.length > 1 && (
                <svg
                  className={`pointer-events-none absolute inset-0 z-20 h-full w-full overflow-visible transition-opacity duration-300 ${
                    isPathFading ? "opacity-0" : "opacity-100"
                  }`}
                >
                  <defs>
                    <linearGradient
                      id="jelly-line-gradient"
                      gradientUnits="userSpaceOnUse"
                      x1={gradientStartPoint?.x ?? 0}
                      y1={gradientStartPoint?.y ?? 0}
                      x2={gradientEndPoint?.x ?? 0}
                      y2={gradientEndPoint?.y ?? 0}
                    >
                      <stop offset="0%" stopColor="#f9c6dc" />
                      <stop offset="48%" stopColor="#fde68a" />
                      <stop offset="100%" stopColor="#bae6fd" />
                    </linearGradient>
                    <linearGradient
                      id="jelly-line-core-gradient"
                      gradientUnits="userSpaceOnUse"
                      x1={gradientStartPoint?.x ?? 0}
                      y1={gradientStartPoint?.y ?? 0}
                      x2={gradientEndPoint?.x ?? 0}
                      y2={gradientEndPoint?.y ?? 0}
                    >
                      <stop offset="0%" stopColor="#fffdfd" />
                      <stop offset="50%" stopColor="#ffffff" />
                      <stop offset="100%" stopColor="#f8fdff" />
                    </linearGradient>
                    <filter id="jelly-glow" x="-40%" y="-40%" width="180%" height="180%">
                      <feGaussianBlur in="SourceGraphic" stdDeviation="5.5" result="jelly-blur" />
                    </filter>
                  </defs>
                  <polyline
                    points={linePath}
                    fill="none"
                    stroke="url(#jelly-line-gradient)"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth="16"
                    filter="url(#jelly-glow)"
                    className="jelly-link-line jelly-link-line--glow"
                    style={{
                      "--line-length": `${lineLength}px`
                    } as CSSProperties}
                  />
                  <polyline
                    points={linePath}
                    fill="none"
                    stroke="#f9c6dc"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth="10"
                    className="jelly-link-line jelly-link-line--support"
                    style={{
                      "--line-length": `${lineLength}px`
                    } as CSSProperties}
                  />
                  <polyline
                    points={linePath}
                    fill="none"
                    stroke="url(#jelly-line-core-gradient)"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth="6"
                    className="jelly-link-line jelly-link-line--core"
                    style={{
                      "--line-length": `${lineLength}px`
                    } as CSSProperties}
                  />
                </svg>
              )}
              {board.flat().map((cell) => {
                const cellKey = getCellKey(cell.row, cell.col);
                return (
                  <GameTile
                    key={cellKey}
                    cell={cell}
                    cellKey={cellKey}
                    isHinted={hintCellKeys.has(cellKey)}
                    isInteractionDisabled={isInteractionDisabled}
                    onCellClick={handleCellClick}
                    setCellRef={setCellRef}
                  />
                );
              })}
            </div>
          </div>

          <aside className="space-y-4 rounded-[28px] border border-white/65 bg-white/48 p-6 backdrop-blur-xl">
            <div>
              <p className="text-xs uppercase tracking-[0.35em] text-rose-400">Game Guide</p>
              <h2 className="mt-2 text-2xl font-bold text-slate-900">游戏提示</h2>
            </div>
            <div className="space-y-3 text-sm leading-6 text-slate-700">
              <p>不同难度会改变棋盘尺寸和最大拐弯次数，系统会按当前规则生成对应局面。</p>
              <p>提示按钮会高亮一组可消图块，死局时系统会自动重排剩余图块。</p>
              <p>清空全部图案即可完成本局挑战，重新生成会保持当前难度不变。</p>
            </div>
            {deadlockNotice ? (
              <div className="rounded-2xl border border-amber-200/90 bg-gradient-to-r from-amber-50 via-rose-50 to-sky-50 px-4 py-4 text-sm leading-6 text-amber-900 shadow-[0_8px_24px_rgba(251,191,36,0.16)]">
                当前已经没有可以被消除的图块，系统已自动进行重排。
              </div>
            ) : null}
            <div className="rounded-2xl border border-white/60 bg-pink-50/75 px-4 py-4 text-sm text-slate-700">
              {statusMessage}
            </div>
            <div className="rounded-2xl border border-white/60 bg-sky-50/75 px-4 py-4 text-sm text-slate-700">
              连线轨迹：
              {pathPreview.length > 0 ? (
                <span className="ml-2 text-rose-400">
                  {pathPreview.map((point) => `(${point.row}, ${point.col})`).join(" -> ")}
                </span>
              ) : (
                <span className="ml-2 text-slate-400">等待本次配对结果</span>
              )}
            </div>
          </aside>
        </section>
      </div>
    </main>
  );
}
