import { useCallback, useEffect, useMemo, useState } from "react";
import { getDashboardApi, getPendingAPi } from "../../../api/dashboards";

export type ComingReturn = {
  id: number;
  amount?: number | null;
  dueAt: string;
  role: "CREDITOR" | "DEBTOR";
  itemTitle: string;
  itemDescription?: string;
  itemFileUrl?: string;
  daysLeft: number;
};

export type OverDue = {
  id: number;
  amount?: number | null;
  dueAt: string;
  role: "CREDITOR" | "DEBTOR";
  itemTitle: string;
  itemDescription?: string;
  itemFileUrl?: string;
  overDays: number;
};

export type PendingItem = {
  source: "AGREEMENT" | string;
  id: number;
  status: string;
  pendingUser: string;
  description: string;
  cursorKey: number;
  createdAt?: string;
};

const normalize = (v: any) => (Array.isArray(v) ? v : v ? [v] : []);

export default function useHomeScreen() {
  const [name, setName] = useState<string>("");
  const [borrowedCount, setBorrowedCount] = useState<number>(0);
  const [lentCount, setLentCount] = useState<number>(0);
  const [comingReturns, setComingReturns] = useState<ComingReturn[]>([]);
  const [overDues, setOverDues] = useState<OverDue[]>([]);
  const [showAllComing, setShowAllComing] = useState(false);
  const [showAllOverdue, setShowAllOverdue] = useState(false);
  const [pendingCount, setPendingCount] = useState<number>(0);

  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // 승인 모달/리스트
  const [pendingOpen, setPendingOpen] = useState(false);
  const [pendings, setPendings] = useState<PendingItem[]>([]);
  const [pendingHasNext, setPendingHasNext] = useState<boolean>(true);
  const [pendingLastId, setPendingLastId] = useState<number | null>(null);
  const [pendingLoading, setPendingLoading] = useState<boolean>(false);

  const pendingFetcher = useMemo(() => getPendingAPi(), []);

  // 대시보드 최초 로드 (원래 로직 그대로)
  useEffect(() => {
    const fetchDashboard = async () => {
      try {
        const res = await getDashboardApi();
        const fetchedData = res.data.data;

        setName(
          fetchedData?.name ||
            fetchedData?.userName ||
            fetchedData?.username ||
            ""
        );

        const countsData = fetchedData?.counts;
        if (Array.isArray(countsData)) {
          for (let c of countsData) {
            if (c.role === "CREDITOR") setLentCount(c.count);
            else setBorrowedCount(c.count);
          }
        } else if (countsData && typeof countsData === "object") {
          if (countsData.role === "CREDITOR")
            setLentCount(countsData.count || 0);
        }

        setPendingCount(Number(fetchedData?.pendingCount) || 0);
        setComingReturns(normalize(fetchedData?.comingReturns).slice(0, 5));
        setOverDues(
          normalize(fetchedData?.overDues || fetchedData?.overdues).slice(0, 5)
        );
      } catch {
        setError("대시보드 데이터를 불러오지 못했습니다.");
      } finally {
        setIsLoading(false);
      }
    };
    fetchDashboard();
  }, []);

  const loadPendings = useCallback(
    async (initial = false) => {
      if (pendingLoading) return;
      setPendingLoading(true);
      try {
        const res = await pendingFetcher(
          initial ? undefined : pendingLastId ?? undefined
        );
        const data = res.data?.data || res.data || {};
        const items: PendingItem[] = data.items || [];
        const nextHas = !!data.hasNext;

        const nextLast =
          typeof data.lastId === "number" && data.lastId > 0
            ? data.lastId
            : items.length > 0
            ? items[items.length - 1].cursorKey
            : pendingLastId;

        setPendings((prev) => (initial ? items : [...prev, ...items]));
        setPendingHasNext(nextHas);
        setPendingLastId(nextLast ?? null);
      } catch {
        // noop
      } finally {
        setPendingLoading(false);
      }
    },
    [pendingFetcher, pendingLastId, pendingLoading]
  );

  const openPending = useCallback(async () => {
    setPendingOpen(true);
    if (pendings.length === 0) {
      await loadPendings(true);
    }
  }, [pendings.length, loadPendings]);

  const visibleComing = useMemo(
    () => (showAllComing ? comingReturns : comingReturns.slice(0, 2)),
    [showAllComing, comingReturns]
  );
  const visibleOverdues = useMemo(
    () => (showAllOverdue ? overDues : overDues.slice(0, 2)),
    [showAllOverdue, overDues]
  );

  return {
    name,
    borrowedCount,
    lentCount,
    comingReturns,
    overDues,
    pendingCount,
    showAllComing,
    setShowAllComing,
    showAllOverdue,
    setShowAllOverdue,
    visibleComing,
    visibleOverdues,
    isLoading,
    error,
    pendingOpen,
    setPendingOpen,
    pendings,
    pendingHasNext,
    pendingLoading,
    loadPendings,
    openPending,
  };
}
