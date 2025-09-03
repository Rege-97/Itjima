import { useFocusEffect } from "@react-navigation/native";
import { useCallback, useState } from "react";
import { getItemCountApi, getMyItemsApi } from "../../../../api/items";

export const useItem = (navigation: any) => {
  const [items, setItems] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [lastId, setLastId] = useState<number | null>(null);
  const [hasNext, setHasNext] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [counts, setCounts] = useState<any>({});
  const [activeFilter, setActiveFilter] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState<string>("");

  const fetchInitialItems = async (
    filter: string | null,
    keyword: string | null
  ) => {
    setIsLoading(true);
    setHasNext(true);
    try {
      const [itemsResponse, countsResponse] = await Promise.all([
        getMyItemsApi(undefined, filter!, keyword!),
        getItemCountApi(),
      ]);

      const fetchedData = itemsResponse.data.data;
      setItems(fetchedData.items || []);
      setCounts(countsResponse.data.data || {});
      setLastId(fetchedData.lastId);
      setHasNext(fetchedData.hasNext);
    } catch (error) {
      console.error("내 물품 목록을 불러오는데 실패했습니다:", error);
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

  const handleFilterPress = (status: string | null) => {
    setActiveFilter(status);
    fetchInitialItems(status, searchQuery);
  };

  const fetchMoreItems = async () => {
    if (!hasNext || isLoadingMore) return;

    setIsLoadingMore(true);
    try {
      const response = await getMyItemsApi(
        lastId!,
        activeFilter!,
        searchQuery!
      );
      const fetchedData = response.data.data;
      setItems((prevItems) => [...prevItems, ...(fetchedData.items || [])]);
      setLastId(fetchedData.lastId);
      setHasNext(fetchedData.hasNext);
    } catch (error) {
      console.error("추가 물품 목록을 불러오는데 실패했습니다:", error);
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
    items,
    isLoading,
    isRefreshing,
    isLoadingMore,
    hasNext,
    counts,
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
