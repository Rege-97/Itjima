import React, { useCallback, useState } from "react";
import { getItemCountApi, getMyItemsApi } from "../../api/items";
import { useFocusEffect } from "@react-navigation/native";
import {
  FlatList,
  Image,
  RefreshControl,
  StyleSheet,
  TouchableOpacity,
  View,
} from "react-native";
import { ActivityIndicator, Divider, FAB, Text } from "react-native-paper";
import { MaterialCommunityIcons } from "@expo/vector-icons";

interface SummaryBoxProps {
  counts: {
    itemAllCount?: number;
    itemLoanCount?: number;
    itemAvailableCount?: number;
  };
  activeFilter: string | null;
  onFilterPress: (status: string | null) => void;
}

const SummaryBox = ({ counts, activeFilter, onFilterPress }: SummaryBoxProps) => {
  const total = counts.itemAllCount || 0;
  const onLoan = counts.itemLoanCount || 0;
  const available = counts.itemAvailableCount || 0;

  return (
    <View style={styles.summaryContainer}>
      <TouchableOpacity
        onPress={() => onFilterPress(null)}
        style={styles.summaryItem}
      >
        <Text style={styles.summaryLabel}>총 물건</Text>
        <Text
          style={[
            styles.summaryValue,
            activeFilter === null && styles.activeFilter,
          ]}
        >
          {total}개
        </Text>
      </TouchableOpacity>
      <TouchableOpacity
        onPress={() => onFilterPress("ON_LOAN")}
        style={styles.summaryItem}
      >
        <Text style={styles.summaryLabel}>대여중</Text>
        <Text
          style={[
            styles.summaryValue,
            { color: "#F44336" },
            activeFilter === "ON_LOAN" && styles.activeFilter,
          ]}
        >
          {onLoan}개
        </Text>
      </TouchableOpacity>
      <TouchableOpacity
        onPress={() => onFilterPress("AVAILABLE")}
        style={styles.summaryItem}
      >
        <Text style={styles.summaryLabel}>사용가능</Text>
        <Text
          style={[
            styles.summaryValue,
            { color: "#4CAF50" },
            activeFilter === "AVAILABLE" && styles.activeFilter,
          ]}
        >
          {available}개
        </Text>
      </TouchableOpacity>
    </View>
  );
};

const MyItemsScreen = ({ navigation }: any) => {
  const [items, setItems] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [lastId, setLastId] = useState<number | null>(null);
  const [hasNext, setHasNext] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [counts, setCounts] = useState<any>({});
  const [activeFilter, setActiveFilter] = useState<string | null>(null);

  const fetchInitialItems = async (filter: string | null) => {
    setIsLoading(true);
    setHasNext(true);
    try {
      const [itemsResponse, countsResponse] = await Promise.all([
        getMyItemsApi(undefined, filter || undefined),
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

const handleFilterPress = (status: string | null) => {
    setActiveFilter(status);
    fetchInitialItems(status);
  };

  const fetchMoreItems = async () => {
    if (!hasNext || isLoadingMore) return;

    setIsLoadingMore(true);
    try {
      const response = await getMyItemsApi(lastId!, activeFilter || undefined);
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
      fetchInitialItems(activeFilter);
    }, [])
  );
  const onRefresh = () => {
    if (isRefreshing) return;
    setIsRefreshing(true);
    fetchInitialItems(activeFilter);
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
        case "PENDING_APPROVAL":
        backgroundColor = "#F44336";
        break;
    }
    return (
      <View style={[styles.statusBadge, { backgroundColor }]}>
        <Text style={[styles.statusBadgeText, { color: textColor }]}>
          {status === "AVAILABLE"
            ? "사용가능"
            : status === "ON_LOAN" || status === "PENDING_APPROVAL"
            ? "대여중"
            : status}
        </Text>
      </View>
    );
  };

  if (isLoading) {
    return (
     <View style={{ flex: 1, justifyContent: "center", alignItems: "center" }}>
      <ActivityIndicator animating={true} size="large" />
    </View>
    );
  }

  return (
    <View style={styles.container}>
      <FlatList
        ListHeaderComponent={
          <SummaryBox
            counts={counts}
            activeFilter={activeFilter}
            onFilterPress={handleFilterPress}
          />
        }
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
                </View>
                <Text style={styles.subtitle} numberOfLines={2}>
                  {item.description}
                </Text>

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
    paddingVertical: 8,
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
    justifyContent: "flex-start",
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
    marginTop: 6,
    marginBottom: 40,
    marginLeft: 1,
  },

  statsRow: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    marginTop: 8,
    paddingRight: 8,
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
    marginTop:14,
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
  summaryContainer: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    padding: 16,
    backgroundColor: '#f7f7f7',
    borderBottomWidth: 1,
    borderBottomColor: '#eee',
    marginBottom: 8,
  },
  summaryItem: {
    alignItems: 'center',
  },
  summaryLabel: {
    fontSize: 14,
    color: '#555',
    marginBottom: 4,
  },
  summaryValue: {
    fontSize: 18,
    fontWeight: 'bold',
  },
  activeFilter: {
    textDecorationLine: 'underline',
  },
});

export default MyItemsScreen;
