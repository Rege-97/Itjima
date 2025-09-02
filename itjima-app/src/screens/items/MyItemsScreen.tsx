import React, { useCallback, useState } from "react";
import { getItemCountApi, getMyItemsApi } from "../../api/items";
import { useFocusEffect } from "@react-navigation/native";
import {
  FlatList,
  RefreshControl,
  SafeAreaView,
  StyleSheet,
  View,
} from "react-native";
import {
  ActivityIndicator,
  FAB,
  Searchbar,
  Text,
} from "react-native-paper";
import SummaryBox from "./components/SummaryBox";
import ItemCard from "./components/ItemCard";



const MyItemsScreen = ({ navigation }: any) => {
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

  if (isLoading) {
    return (
      <View style={{ flex: 1, justifyContent: "center", alignItems: "center" }}>
        <ActivityIndicator animating={true} size="large" />
      </View>
    );
  }

  return (
    <SafeAreaView style={styles.container}>
        <View>
            <Text style={{fontSize:24, fontWeight:"bold", margin:16}}>내 물품</Text>
        </View>
      <FlatList
        ListHeaderComponent={
          <>
           <Searchbar
              placeholder="물품명으로 검색"
              value={searchQuery}
              onChangeText={handleSearchChange}
              onSubmitEditing={handleSearchSubmit}
              onClearIconPress={() => {
                setSearchQuery("");
                fetchInitialItems(activeFilter, "");
              }}
              style={styles.searchbar}
            />
            <SummaryBox
              counts={counts}
              activeFilter={activeFilter}
              onFilterPress={handleFilterPress}
            />
          </>
        }
        data={items}
        keyExtractor={(item) => item.id.toString()}
        renderItem={({ item }) => <ItemCard item={item} navigation={navigation} />}
        contentContainerStyle={{ padding: 8 }}
        refreshControl={
          <RefreshControl refreshing={isRefreshing} onRefresh={onRefresh} />
        }
        onEndReached={fetchMoreItems}
        onEndReachedThreshold={0.2}
        ListFooterComponent={
          hasNext && isLoadingMore ? (
            <View style={{ padding: 20 }}>
              <ActivityIndicator animating={true} />
            </View>
          ) : null
        }
      />

      <FAB icon="plus" color="#fff" style={styles.fab} onPress={() => {}} label="물품 추가" />
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#fff",
  },
  
  fab: {
    position: "absolute",
    right: 16,
    bottom: 16,
    backgroundColor: "#6200ee",
  },
  searchbar: {
    backgroundColor: "#ffffffff",
    borderRadius: 0,
  },
});

export default MyItemsScreen;
