import { useFocusEffect } from "@react-navigation/native";
import { useCallback, useState } from "react";
import { getMyAgreementsApi } from "../../../../api/agreements";

export const useAgreement = (navigator: any) => {
  const [agreements, setAgreements] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [lastId, setLastId] = useState<number | null>(null);
  const [hasNext, setHasNext] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [activeFilter, setActiveFilter] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState<string>("");

  const fetchInitialItems = async (
    filter: string | null,
    keyword: string | null
  ) => {
    setIsLoading(true);
    setHasNext(true);
    try {
      const response = await getMyAgreementsApi(undefined, filter!, keyword!);

      const fetchedData = response.data.data;

      setAgreements(fetchedData.items || []);
      setLastId(fetchedData.lastId);
      setHasNext(fetchedData.hasNext);
    } catch (error) {
      console.error("내 대여 목록을 불러오는데 실패했습니다:", error);
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  };

  const handleSearchChange = (query: string) => {
    setSearchQuery(query);
  };

  const handleSearchSubmit = () => {
    fetchInitialItems(activeFilter, searchQuery);
  };

  const handleFilterPress = (role: string | null) => {
    const nextFilter = role === "ALL" ? null : role;
    setActiveFilter(nextFilter);
    fetchInitialItems(nextFilter, searchQuery);
  };

  const fetchMoreItems = async () => {
    if (!hasNext || isLoadingMore) return;

    setIsLoadingMore(true);
    try {
      const response = await getMyAgreementsApi(
        lastId!,
        activeFilter!,
        searchQuery!
      );

      const fetchedData = response.data.data;
      setAgreements((prevAgreements) => [
        ...prevAgreements,
        ...(fetchedData.items || []),
      ]);
      setLastId(fetchedData.lastId);
      setHasNext(fetchedData.hasNext);
    } catch (error) {
      console.error("추가 대여 목록을 불러오는데 실패했습니다:", error);
    } finally {
      setIsLoadingMore(false);
    }
  };

  useFocusEffect(
    useCallback(() => {
      fetchInitialItems(activeFilter, searchQuery);
    }, [activeFilter])
  );
  const onRefresh = () => {
    if (isRefreshing) return;
    setIsRefreshing(true);
    fetchInitialItems(activeFilter, searchQuery);
  };

  return {
    agreements,
    isLoading,
    isRefreshing,
    isLoadingMore,
    hasNext,
    searchQuery,
    activeFilter,
    fetchInitialItems,
    setSearchQuery,
    handleSearchChange,
    handleSearchSubmit,
    handleFilterPress,
    fetchMoreItems,
    onRefresh,
  };
};
