import React, { useCallback, useState } from "react";
import { getMyItemsApi } from "../../api/items";
import { useFocusEffect } from "@react-navigation/native";
import {
  FlatList,
  Image,
  RefreshControl,
  StyleSheet,
  View,
} from "react-native";
import {
  ActivityIndicator,
  Card,
  Divider,
  FAB,
  Text,
} from "react-native-paper";
import { MaterialCommunityIcons } from "@expo/vector-icons";

const MyItemsScreen = ({ navigation }: any) => {
  const [items, setItems] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [lastId, setLastId] = useState<number | null>(null);
  const [hasNext, setHasNext] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);

  const fetchInitialItems = async () => {
    setIsLoading(true);
    setHasNext(true);
    try {
      const response = await getMyItemsApi();
      const fetchedData = response.data.data;
      setItems(fetchedData.items || []);
      setLastId(fetchedData.lastId);
      setHasNext(fetchedData.hasNext);
    } catch (error) {
      console.error("내 물품 목록을 불러오는데 실패했습니다:", error);
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  };

  const fetchMoreItems = async () => {
    if (!hasNext || isLoadingMore) return;

    setIsLoadingMore(true);
    try {
      const response = await getMyItemsApi(lastId!);
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
      fetchInitialItems();
    }, [])
  );
  const onRefresh = () => {
    if (isRefreshing) return;
    setIsRefreshing(true);
    fetchInitialItems();
  };

  const getItemStatusBadge = (status: string) => {
    let backgroundColor = "#ccc";
    let textColor = "#fff";

    switch (status) {
      case "AVAILABLE":
        backgroundColor = "#4CAF50";
        break;
      case "ON_LOAN":
        backgroundColor = "#F44336";
        break;
    }
    return (
      <View style={[styles.statusBadge, { backgroundColor }]}>
        <Text style={[styles.statusBadgeText, { color: textColor }]}>
          {status === "AVAILABLE"
            ? "사용가능"
            : status === "ON_LOAN"
            ? "대여중"
            : status}
        </Text>
      </View>
    );
  };

  if (isLoading) {
    return (
      <View>
        <ActivityIndicator animating={true} size="large" />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <FlatList
        data={items}
        keyExtractor={(item) => item.id.toString()}
        renderItem={({ item }) => (
          <View style={styles.listItem}>
            <View style={styles.headerRow}>
              <Image
                source={{
                  uri: item.fileUrl || "https://via.placeholder.com/150",
                }}
                style={styles.squareAvatar}
              />
              <View style={styles.rightCol}>
                <View style={styles.titleRow}>
                  <Text style={styles.title} numberOfLines={1}>
                    {item.title}
                  </Text>
                  {getItemStatusBadge(item.status)}
                  <Text style={styles.subtitle} numberOfLines={2}>
                    {item.description}
                  </Text>
                </View>

                <View style={styles.statsRow}>
                  <View style={styles.statItem}>
                    <MaterialCommunityIcons
                      name="account-supervisor-outline"
                      size={15}
                      color="#555"
                    />
                    <Text style={styles.statText}>{item.loanCount}회 대여</Text>
                  </View>

                  <Text style={styles.lastLoanText}>
                    마지막 대여: {item.lastDebtorName || "없음"}
                  </Text>
                </View>
              </View>
            </View>

            <Divider style={styles.divider} />
          </View>
        )}
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

      <FAB icon="plus" style={styles.fab} onPress={() => {}} />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#fff",
  },
  listItem: {
    paddingVertical: 12,
    paddingHorizontal: 4,
    backgroundColor: "#fff",
  },

  headerRow: {
    flexDirection: "row",
    alignItems: "flex-start",
  },
  squareAvatar: {
    width: 120,
    height: 120,
    borderRadius: 8,
    backgroundColor: "#eee",
    marginRight: 20,
  },
  rightCol: {
    flex: 1,
    justifyContent: "space-between",
    height: 120,
    paddingVertical: 4,
  },

  titleRow: {
    flexDirection: "row",
    alignItems: "center",
    flexWrap: "wrap",
  },
  title: {
    fontSize: 16,
    fontWeight: "bold",
    marginRight: 8,
  },
  subtitle: {
    fontSize: 12,
    color: "#7c7c7c",
    marginTop: 15,
    marginLeft: 1,
  },

  statsRow: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    marginTop: 8,
  },
  statItem: {
    flexDirection: "row",
    alignItems: "center",
  },
  statText: {
    marginLeft: 4,
    color: "#747474",
    fontSize: 12,
  },
  lastLoanText: {
    color: "#747474",
    fontSize: 12,
  },

  divider: {
    marginTop: 12,
  },

  statusBadge: {
    borderRadius: 6,
    paddingHorizontal: 8,
    paddingVertical: 4,
    width: 60,
    alignItems: "center",
  },
  statusBadgeText: {
    fontSize: 12,
    fontWeight: "bold",
    color: "#fff",
  },

  fab: {
    position: "absolute",
    right: 16,
    bottom: 16,
  },
});

export default MyItemsScreen;
