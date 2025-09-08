import { useCallback, useState } from "react";
import { Alert } from "react-native";
import {
  getItemAgreementHistoryApi,
  getItemDetailApi,
} from "../../../../api/items";
import { useFocusEffect } from "@react-navigation/native";

export const useItemDetails = (itemId: number, navigation: any) => {
  const [item, setItem] = useState<any>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [agreementHistory, setAgreementHistory] = useState<any[]>([]);
  const [lastId, setLastId] = useState<number | null>(null);
  const [hasNext, setHasNext] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [isReady, setIsReady] = useState(false);

  const fetchInitialData = useCallback(async () => {
    if (itemId === undefined || itemId === null) {
      Alert.alert("오류", "유효하지 않은 물품 정보입니다.");
      navigation.goBack();
      return;
    }

    setIsLoading(true);
    setIsReady(false);
    try {
      const [itemDetailRes, historyRes] = await Promise.all([
        getItemDetailApi(itemId),
        getItemAgreementHistoryApi(itemId),
      ]);
      setItem(itemDetailRes.data.data);
      const historyData = historyRes.data.data;
      setAgreementHistory(historyData.items || []);
      setLastId(historyData.lastId);
      setHasNext(historyData.hasNext);
    } catch (error) {
      console.error("물품 정보를 불러오는 데 실패했습니다:", error);
      Alert.alert("오류", "물품 정보를 불러올 수 없습니다.");
      navigation.goBack();
    } finally {
      setIsLoading(false);
      setIsReady(true);
    }
  }, [itemId, navigation]);

  const fetchMoreData = useCallback(async () => {
    if (!isReady || !hasNext || isLoadingMore) return;

    setIsLoadingMore(true);
    try {
      const response = await getItemAgreementHistoryApi(itemId, lastId!);
      const newItems = response.data.data.items || [];
      setAgreementHistory((prev) => [...prev, ...newItems]);
      setLastId(response.data.data.lastId);
      setHasNext(response.data.data.hasNext);
    } catch (error) {
      console.error("대여 이력을 추가로 불러오는 데 실패했습니다:", error);
    } finally {
      setIsLoadingMore(false);
    }
  }, [isReady, hasNext, isLoadingMore, itemId, lastId]);

  useFocusEffect(
    useCallback(() => {
      fetchInitialData();
    }, [fetchInitialData])
  );

  return {
    item,
    isLoading,
    agreementHistory,
    hasNext,
    isLoadingMore,
    fetchMoreData,
  };
};
