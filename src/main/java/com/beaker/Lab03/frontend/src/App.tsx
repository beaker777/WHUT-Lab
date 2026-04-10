import { useEffect, useMemo, useState } from "react";
import { PauseCircle, PartyPopper, Sparkles, TimerReset } from "lucide-react";
import { HappyLinkGame } from "./components/HappyLinkGame";

type ScreenState = "landing" | "playing" | "paused" | "victory";

function formatElapsedTime(totalSeconds: number) {
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  return `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;
}

export default function App() {
  const [screenState, setScreenState] = useState<ScreenState>("landing");
  const [elapsedSeconds, setElapsedSeconds] = useState(0);
  const [roundSeed, setRoundSeed] = useState(0);

  useEffect(() => {
    if (screenState !== "playing") {
      return undefined;
    }

    const timer = window.setInterval(() => {
      setElapsedSeconds((currentSeconds) => currentSeconds + 1);
    }, 1000);

    return () => {
      window.clearInterval(timer);
    };
  }, [screenState, roundSeed]);

  const formattedElapsedTime = useMemo(
    () => formatElapsedTime(elapsedSeconds),
    [elapsedSeconds]
  );

  const startNewRound = () => {
    setElapsedSeconds(0);
    setRoundSeed((currentSeed) => currentSeed + 1);
    setScreenState("playing");
  };

  const handleVictory = () => {
    setScreenState("victory");
  };

  const handlePause = () => {
    setScreenState("paused");
  };

  const handleResume = () => {
    setScreenState("playing");
  };

  const returnToLanding = () => {
    setScreenState("landing");
    setElapsedSeconds(0);
  };

  if (screenState === "landing") {
    return (
      <main className="relative min-h-screen overflow-hidden bg-gradient-to-br from-pink-100 via-amber-50 to-sky-100 px-4 py-8 text-slate-800">
        <div className="pointer-events-none absolute inset-0 overflow-hidden">
          <div className="absolute left-[-4rem] top-12 h-40 w-40 rounded-full bg-pink-200/60 blur-3xl md:h-56 md:w-56" />
          <div className="absolute right-[8%] top-24 h-44 w-44 rounded-full bg-teal-100/60 blur-3xl md:h-64 md:w-64" />
          <div className="absolute bottom-16 left-[12%] h-48 w-48 rounded-full bg-sky-200/60 blur-3xl md:h-72 md:w-72" />
        </div>

        <div className="relative z-10 mx-auto flex min-h-[calc(100vh-4rem)] w-full max-w-[1200px] items-center justify-center rounded-[32px] border border-white/60 bg-white/50 p-6 shadow-[0_8px_32px_rgba(255,192,203,0.15)] backdrop-blur-xl lg:p-10">
          <section className="mx-auto flex w-full max-w-3xl flex-col items-center justify-center gap-8 text-center">
            <div className="space-y-4">
              <p className="text-sm uppercase tracking-[0.45em] text-rose-400">Macaron Arcade</p>
              <h1 className="text-5xl font-black tracking-[0.18em] text-slate-900 md:text-6xl">
                欢乐连连看
              </h1>
              <p className="mx-auto max-w-2xl text-base leading-7 text-slate-700 md:text-lg">
                在清爽柔和的马卡龙棋盘中，找出可以连接的图案组合，挑战更快完成整局清屏。
              </p>
            </div>

            <div className="grid w-full max-w-2xl gap-4 rounded-[28px] border border-white/70 bg-white/60 p-6 text-sm text-slate-700 shadow-[0_16px_48px_rgba(248,200,220,0.18)] backdrop-blur-xl md:grid-cols-3">
              <div className="rounded-2xl bg-pink-50/80 px-4 py-5">
                <Sparkles className="mx-auto mb-3 h-6 w-6 text-rose-400" />
                最多两次拐弯
              </div>
              <div className="rounded-2xl bg-amber-50/80 px-4 py-5">
                <TimerReset className="mx-auto mb-3 h-6 w-6 text-amber-400" />
                开始后自动计时
              </div>
              <div className="rounded-2xl bg-sky-50/80 px-4 py-5">
                <PartyPopper className="mx-auto mb-3 h-6 w-6 text-sky-400" />
                清空棋盘赢得庆祝
              </div>
            </div>

            <button
              type="button"
              onClick={startNewRound}
              className="rounded-full border border-white/80 bg-gradient-to-r from-pink-200 via-amber-100 to-sky-100 px-12 py-5 text-lg font-bold text-slate-900 shadow-[0_18px_48px_rgba(248,200,220,0.35)] transition duration-200 hover:scale-[1.03] hover:shadow-[0_24px_60px_rgba(205,231,255,0.42)] focus:outline-none focus:ring-4 focus:ring-pink-200/80"
            >
              开始游戏
            </button>
          </section>
        </div>
      </main>
    );
  }

  if (screenState === "victory") {
    return (
      <main className="relative min-h-screen overflow-hidden bg-gradient-to-br from-pink-100 via-amber-50 to-sky-100 px-4 py-8 text-slate-800">
        <div className="pointer-events-none absolute inset-0 overflow-hidden">
          <div className="absolute left-[8%] top-[16%] h-28 w-28 rounded-full bg-pink-200/60 blur-2xl animate-[macaronFloat_7s_ease-in-out_infinite]" />
          <div className="absolute right-[12%] top-[12%] h-24 w-24 rounded-full bg-amber-200/70 blur-2xl animate-[macaronFloat_9s_ease-in-out_infinite]" />
          <div className="absolute bottom-[18%] left-[18%] h-32 w-32 rounded-full bg-sky-200/60 blur-3xl animate-[macaronFloat_10s_ease-in-out_infinite]" />
          <div className="absolute bottom-[20%] right-[16%] h-24 w-24 rounded-full bg-emerald-100/70 blur-2xl animate-[macaronFloat_8.5s_ease-in-out_infinite]" />
        </div>

        <div className="relative z-10 mx-auto flex min-h-[calc(100vh-4rem)] w-full max-w-[1200px] items-center justify-center rounded-[32px] border border-white/60 bg-white/50 p-6 shadow-[0_8px_32px_rgba(255,192,203,0.15)] backdrop-blur-xl lg:p-10">
          <section className="mx-auto flex w-full max-w-2xl flex-col items-center gap-8 rounded-[32px] border border-white/70 bg-white/70 px-8 py-10 text-center shadow-[0_18px_56px_rgba(248,200,220,0.22)] backdrop-blur-xl">
            <div className="inline-flex items-center gap-3 rounded-full bg-pink-50/90 px-5 py-2 text-sm font-semibold tracking-[0.3em] text-rose-400">
              <PartyPopper className="h-5 w-5" />
              VICTORY
            </div>

            <div className="space-y-4">
              <h2 className="text-4xl font-black tracking-[0.16em] text-slate-900 md:text-5xl">
                恭喜通关
              </h2>
              <p className="text-base leading-7 text-slate-700 md:text-lg">
                这一局你已经成功清空全部图案，整张棋盘被你漂亮地拿下了。
              </p>
            </div>

            <div className="grid w-full gap-4 md:grid-cols-2">
              <div className="rounded-[28px] border border-white/70 bg-pink-50/85 px-6 py-6">
                <p className="text-xs uppercase tracking-[0.35em] text-rose-400">Completion Time</p>
                <p className="mt-3 text-4xl font-black text-slate-900">{formattedElapsedTime}</p>
              </div>
              <div className="rounded-[28px] border border-white/70 bg-sky-50/85 px-6 py-6">
                <p className="text-xs uppercase tracking-[0.35em] text-sky-400">Round Status</p>
                <p className="mt-3 text-2xl font-bold text-slate-900">全图案已清空</p>
              </div>
            </div>

            <div className="flex flex-col items-center gap-4 sm:flex-row">
              <button
                type="button"
                onClick={startNewRound}
                className="rounded-full border border-white/80 bg-gradient-to-r from-pink-200 via-amber-100 to-sky-100 px-10 py-4 text-base font-bold text-slate-900 shadow-[0_18px_48px_rgba(248,200,220,0.35)] transition duration-200 hover:scale-[1.03] hover:shadow-[0_24px_60px_rgba(205,231,255,0.42)] focus:outline-none focus:ring-4 focus:ring-pink-200/80"
              >
                再来一局
              </button>
              <button
                type="button"
                onClick={() => setScreenState("landing")}
                className="rounded-full border border-white/80 bg-white/80 px-10 py-4 text-base font-bold text-slate-700 shadow-[0_10px_32px_rgba(255,255,255,0.35)] transition duration-200 hover:scale-[1.03] hover:bg-white focus:outline-none focus:ring-4 focus:ring-sky-100"
              >
                返回首页
              </button>
            </div>
          </section>
        </div>
      </main>
    );
  }

  return (
    <div className="relative">
      <HappyLinkGame
        key={roundSeed}
        elapsedSeconds={elapsedSeconds}
        isPaused={screenState === "paused"}
        onGameWon={handleVictory}
        onPauseRequest={handlePause}
      />

      {screenState === "paused" && (
        <div className="absolute inset-0 z-40 flex items-center justify-center bg-white/20 px-4 backdrop-blur-md">
          <section className="w-full max-w-md rounded-[32px] border border-white/75 bg-white/78 px-8 py-10 text-center shadow-[0_20px_64px_rgba(248,200,220,0.26)]">
            <div className="mx-auto inline-flex items-center gap-3 rounded-full bg-pink-50/90 px-5 py-2 text-sm font-semibold tracking-[0.3em] text-rose-400">
              <PauseCircle className="h-5 w-5" />
              PAUSED
            </div>

            <h2 className="mt-6 text-3xl font-black tracking-[0.14em] text-slate-900">
              游戏已暂停
            </h2>
            <p className="mt-4 text-base leading-7 text-slate-700">
              当前计时已经暂停。你可以继续本局挑战，或者直接返回主界面。
            </p>

            <div className="mt-8 flex flex-col gap-4">
              <button
                type="button"
                onClick={handleResume}
                className="rounded-full border border-white/80 bg-gradient-to-r from-pink-200 via-amber-100 to-sky-100 px-10 py-4 text-base font-bold text-slate-900 shadow-[0_18px_48px_rgba(248,200,220,0.35)] transition duration-200 hover:scale-[1.03] hover:shadow-[0_24px_60px_rgba(205,231,255,0.42)] focus:outline-none focus:ring-4 focus:ring-pink-200/80"
              >
                继续游戏
              </button>
              <button
                type="button"
                onClick={returnToLanding}
                className="rounded-full border border-white/80 bg-white/85 px-10 py-4 text-base font-bold text-slate-700 shadow-[0_10px_32px_rgba(255,255,255,0.35)] transition duration-200 hover:scale-[1.03] hover:bg-white focus:outline-none focus:ring-4 focus:ring-sky-100"
              >
                返回主界面
              </button>
            </div>
          </section>
        </div>
      )}
    </div>
  );
}
