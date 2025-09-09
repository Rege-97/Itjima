import { useCallback, useState } from "react";
import { Alert } from "react-native";
import {
  confirmTransactionApi,
  getAgreementDetailApi,
  getAgreementLogsApi,
  getAgreementTransactionsApi,
  rejectTransactionApi,
} from "../../../../api/agreements";

export const formatDate = (dateString: string | null) => {
  if (!dateString) return "정보 없음";
  const d = new Date(dateString);
  return d.toLocaleString("ko-KR", {
    year: "numeric",
    month: "long",
    day: "numeric",
    hour12: false,
  });
};
export const toLocalDateString = (d: Date) => {
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
};

const dedupeById = (list: any[]) => [
  ...new Map(list.map((i) => [i.id, i])).values(),
];

export default function useAgreementDetail(
  agreementId: number,
  navigation: any
) {
  const [agreement, setAgreement] = useState<any>(null);
  const [isLoading, setIsLoading] = useState(true);

  const [logItems, setLogItems] = useState<any[]>([]);
  const [logHasNext, setLogHasNext] = useState(true);
  const [logLastId, setLogLastId] = useState<number | null>(null);
  const [isLogLoading, setIsLogLoading] = useState(false);

  const [repayItems, setRepayItems] = useState<any[]>([]);
  const [repayHasNext, setRepayHasNext] = useState(true);
  const [repayLastId, setRepayLastId] = useState<number | null>(null);
  const [isRepayLoading, setIsRepayLoading] = useState(false);
  const [repayActingIds, setRepayActingIds] = useState<Set<number>>(new Set());

  const fetchInitialData = useCallback(async () => {
    if (agreementId === undefined || agreementId === null) {
      Alert.alert("오류", "유효하지 않은 대여 정보입니다.");
      navigation.goBack();
      return;
    }
    setIsLoading(true);
    try {
      const { data } = await getAgreementDetailApi(agreementId);
      setAgreement(data?.data ?? data);
    } catch (error) {
      Alert.alert("오류", "대여 정보를 불러올 수 없습니다.");
      navigation.goBack();
    } finally {
      setIsLoading(false);
    }
  }, [agreementId, navigation]);

  const fetchLogs = useCallback(async () => {
    setIsLogLoading(true);
    try {
      const response = await getAgreementLogsApi(agreementId, logLastId!);
      const fetchedData = response.data.data;
      setLogItems(fetchedData.items || []);
      setLogLastId(fetchedData.lastId);
      setLogHasNext(fetchedData.hasNext);
    } catch (error) {
      console.error("활동로그 목록을 불러오는데 실패했습니다:", error);
    } finally {
      setIsLogLoading(false);
    }
  }, [agreementId, logLastId]);

  const fetchRepayments = useCallback(async () => {
    if (isRepayLoading || !repayHasNext) return;
    setIsRepayLoading(true);
    try {
      const { data } = await getAgreementTransactionsApi(
        agreementId,
        repayLastId!
      );
      const payload = data?.data ?? data;

      const nextItems = payload.items || [];
      const merged = dedupeById([...repayItems, ...nextItems]);

      setRepayItems(merged);
      setRepayLastId(payload.lastId ?? null);
      setRepayHasNext(!!payload.hasNext);
    } catch (e) {
      console.error("상환 기록을 불러오는데 실패했습니다:", e);
    } finally {
      setIsRepayLoading(false);
    }
  }, [agreementId, isRepayLoading, repayHasNext, repayLastId, repayItems]);

  const handleConfirmRepay = useCallback(
    async (txId: number) => {
      if (repayActingIds.has(txId)) return;
      setRepayActingIds((prev) => new Set(prev).add(txId));
      try {
        await confirmTransactionApi(txId);
        Alert.alert("승인됨", "상환이 승인되었습니다.");

        setRepayItems([]);
        setRepayLastId(null);
        setRepayHasNext(true);
        await fetchInitialData();
        await fetchRepayments();
      } catch (e) {
        console.error(e);
        Alert.alert("실패", "상환 승인에 실패했습니다.");
      } finally {
        setRepayActingIds((prev) => {
          const next = new Set(prev);
          next.delete(txId);
          return next;
        });
      }
    },
    [fetchInitialData, fetchRepayments, repayActingIds]
  );

  const handleRejectRepay = useCallback(
    async (txId: number) => {
      if (repayActingIds.has(txId)) return;
      setRepayActingIds((prev) => new Set(prev).add(txId));
      try {
        await rejectTransactionApi(txId);
        Alert.alert("거절됨", "상환이 거절되었습니다.");

        setRepayItems([]);
        setRepayLastId(null);
        setRepayHasNext(true);
        await fetchInitialData();
        await fetchRepayments();
      } catch (e) {
        console.error(e);
        Alert.alert("실패", "상환 거절에 실패했습니다.");
      } finally {
        setRepayActingIds((prev) => {
          const next = new Set(prev);
          next.delete(txId);
          return next;
        });
      }
    },
    [fetchInitialData, fetchRepayments, repayActingIds]
  );

  return {
    agreement,
    isLoading,
    logItems,
    logHasNext,
    isLogLoading,
    fetchLogs,
    repayItems,
    repayHasNext,
    isRepayLoading,
    fetchRepayments,
    repayActingIds,
    handleConfirmRepay,
    handleRejectRepay,
    fetchInitialData,
  };
}
