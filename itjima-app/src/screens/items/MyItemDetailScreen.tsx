import React, { useEffect, useState, useCallback } from "react";
import {
  Alert,
  Dimensions,
  FlatList,
  Image,
  SafeAreaView,
  StyleSheet,
  View,
} from "react-native";
import {
  ActivityIndicator,
  Button,
  Card,
  Divider,
  Text,
} from "react-native-paper";
import { getItemAgreementHistoryApi, getItemDetailApi } from "../../api/items";
import { MaterialCommunityIcons } from "@expo/vector-icons";
import StatisticsCard from "./components/StatisticsCard";
import { useFocusEffect } from "@react-navigation/native";

const { width } = Dimensions.get("window");

const formatDate = (dateString: string | null) => {
  if (!dateString) return "정보 없음";
  const date = new Date(dateString);
  return date.toLocaleString("ko-KR", {
    year: "numeric",
    month: "long",
    day: "numeric",
    hour12: false,
  });
};

const StatusBadge = ({ status }: { status: string }) => {
  let backgroundColor = "#ccc";
  let label = status;

  switch (status) {
    case "AVAILABLE":
      backgroundColor = "#4CAF50";
      label = "사용가능";
      break;
    case "ON_LOAN":
      backgroundColor = "#F44336";
      label = "대여중";
      break;
    case "PENDING_APPROVAL":
      backgroundColor = "#5c36f4ff";
      label = "대여요청";
      break;
  }

  return (
    <View style={[styles.statusBadge, { backgroundColor }]}>
      <Text style={styles.statusBadgeText}>{label}</Text>
    </View>
  );
};

const AgreementStatusBadge = ({
  status,
  overdueReturn,
}: {
  status: string;
  overdueReturn: boolean;
}) => {
  let backgroundColor = "#ccc";
  let label = status;

  switch (status) {
    case "PENDING":
      backgroundColor = "#FFC107"; // 노랑
      label = "승인 대기 중";
      break;
    case "ACCEPTED":
      backgroundColor = "#2196F3"; // 파랑
      label = "대여중";
      break;
    case "REJECTED":
      backgroundColor = "#F44336"; // 빨강
      label = "거절됨";
      break;
    case "COMPLETED":
      if (overdueReturn) {
        backgroundColor = "#FF5722"; // 주황/빨강
        label = "연체반납";
      } else {
        backgroundColor = "#4CAF50"; // 초록
        label = "완료됨";
      }
      break;
    case "CANCELED":
      backgroundColor = "#9E9E9E"; // 회색
      label = "취소됨";
      break;
    case "OVERDUE":
      backgroundColor = "#E91E63";
      label = "연체됨";
      break;
  }

  return (
    <View style={[styles.statusBadge, { backgroundColor }]}>
      <Text style={styles.statusBadgeText}>{label}</Text>
    </View>
  );
};

// Header component for the FlatList
const ItemDetailsHeader = ({ item, styles }: any) => {
  const rentalCount = item.rentalCount || 0;
  const totalRentalDays = item.totalRentalDays || 0;
  const avgRentalDays = item.avgRentalDays || 0;

  return (
    <View>
      <Image
        source={{
          uri:
            item.fileUrl || "https://via.placeholder.com/400x300?text=No+Image",
        }}
        style={styles.headerImage}
      />
      <View style={styles.statsContainer}>
        <StatisticsCard
          title="총 대여"
          value={`${rentalCount}회`}
          iconName="account-group"
          iconColor="#4285F4"
        />
        <StatisticsCard
          title="총 대여일"
          value={`${totalRentalDays}일`}
          iconName="calendar-multiple"
          iconColor="#34A853"
        />
        <StatisticsCard
          title="평균 기간"
          value={`${avgRentalDays}일`}
          iconName="chart-line"
          iconColor="#EA4335"
        />
      </View>
      <View style={styles.contentSection}>
        <View style={styles.titleSection}>
          <Text style={styles.title}>{item.title}</Text>
          <StatusBadge status={item.status} />
        </View>
        <View style={styles.infoGroup}>
          <Text style={styles.sectionTitle}>물품 정보</Text>
          <Text style={styles.descriptionText}>
            {item.description || "등록된 설명이 없습니다."}
          </Text>
        </View>
        <View style={styles.infoGroup}>
          <View style={styles.infoRow}>
            <MaterialCommunityIcons
              name="calendar-plus"
              size={20}
              color="#666"
              style={styles.infoIcon}
            />
            <Text style={styles.infoLabel}>등록일</Text>
            <Text style={styles.infoValue}>{formatDate(item.createdAt)}</Text>
          </View>
        </View>
        {(item.currentDebtorName ||
          item.currentStartAt ||
          item.currentDueAt) && (
          <>
            <Divider style={styles.divider} />
            <View style={styles.infoGroup}>
              <Text style={styles.sectionTitle}>현재 대여 상태</Text>
              {item.currentDebtorName && (
                <View style={styles.infoRow}>
                  <MaterialCommunityIcons
                    name="account"
                    size={20}
                    color="#666"
                    style={styles.infoIcon}
                  />
                  <Text style={styles.infoLabel}>대여자</Text>
                  <Text style={styles.infoValue}>{item.currentDebtorName}</Text>
                </View>
              )}
              {item.currentStartAt && (
                <View style={styles.infoRow}>
                  <MaterialCommunityIcons
                    name="calendar-arrow-right"
                    size={20}
                    color="#666"
                    style={styles.infoIcon}
                  />
                  <Text style={styles.infoLabel}>대여 시작일</Text>
                  <Text style={styles.infoValue}>
                    {formatDate(item.currentStartAt)}
                  </Text>
                </View>
              )}
              {item.currentDueAt && (
                <View style={styles.infoRow}>
                  <MaterialCommunityIcons
                    name="calendar-arrow-left"
                    size={20}
                    color="#666"
                    style={styles.infoIcon}
                  />
                  <Text style={styles.infoLabel}>반납 예정일</Text>
                  <Text style={styles.infoValue}>
                    {formatDate(item.currentDueAt)}
                  </Text>
                </View>
              )}
            </View>
          </>
        )}
        <Divider style={styles.divider} />
        <View style={styles.infoGroup}>
          <Text style={styles.sectionTitle}>대여 기록</Text>
        </View>
      </View>
    </View>
  );
};

const MyItemDetailScreen = ({ route, navigation }: any) => {
  const { itemId } = route.params;
  const [item, setItem] = useState<any>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [agreementHistory, setAgreementHistory] = useState<any[]>([]);
  const [lastId, setLastId] = useState<number | null>(null);
  const [hasNext, setHasNext] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);

  const fetchInitialItems = async (id: number) => {
    setIsLoading(true);
    setHasNext(true);
    try {
      const [itemDetailResponse, itemAgreementHistoryResponse] =
        await Promise.all([
          getItemDetailApi(id),
          getItemAgreementHistoryApi(id),
        ]);

      const fetchedItem = itemDetailResponse.data.data;
      const fetchedAgreementHistory = itemAgreementHistoryResponse.data.data;

      setItem(fetchedItem);
      setAgreementHistory(fetchedAgreementHistory.items || []);
      setLastId(fetchedAgreementHistory.lastId);
      setHasNext(fetchedAgreementHistory.hasNext);
    } catch (error) {
      console.error("물품을 불러오는데 실패했습니다:", error);
      Alert.alert("오류", "물품 정보를 불러올 수 없습니다.");
      navigation.goBack();
    } finally {
      setIsLoading(false);
    }
  };

  const fetchMoreAgreementHistory = async () => {
    if (!hasNext || isLoadingMore || itemId === undefined || itemId === null)
      return;

    setIsLoadingMore(true);
    try {
      const response = await getItemAgreementHistoryApi(itemId, lastId!);
      const newItems = response.data.data.items || [];
      setAgreementHistory((prev) => [...prev, ...newItems]);
      setLastId(response.data.data.lastId);
      setHasNext(response.data.data.hasNext);
    } catch (error) {
      console.error("대여 이력을 불러오는 데 실패했습니다:", error);
    } finally {
      setIsLoadingMore(false);
    }
  };

  useFocusEffect(
    useCallback(() => {
      if (itemId !== undefined && itemId !== null) {
        fetchInitialItems(itemId);
      } else {
        Alert.alert("오류", "유효하지 않은 물품 정보입니다.");
        navigation.goBack();
      }
    }, [itemId])
  );

  const renderAgreementItem = useCallback(
    ({ item }: any) => (
      <View style={styles.agreementItemContainer}>
        <View style={styles.agreementHeaderRow}>
          <View style={styles.textBlock}>
            <Text style={styles.agreementItemTitle}>
              {item.debtorName || "이름 정보 없음"}
            </Text>
            <Text style={styles.agreementItemDaysInline}>
              {item.rentalDays}일 대여
            </Text>
          </View>

          <AgreementStatusBadge
            status={item.status}
            overdueReturn={item.overdueReturn}
          />
        </View>

        {/* terms 등 나머지 정보는 그대로 */}
        {item.terms ? (
          <Text style={styles.agreementItemTerms}>{item.terms}</Text>
        ) : null}

        <View style={styles.infoRow}>
          <MaterialCommunityIcons
            name="calendar-arrow-right"
            size={20}
            color="#666"
            style={styles.infoIcon}
          />
          <Text style={styles.infoLabel}>대여 시작일</Text>
          <Text style={styles.infoValue}>{formatDate(item.startDate)}</Text>
        </View>

        <View style={styles.infoRow}>
          <MaterialCommunityIcons
            name="calendar-arrow-left"
            size={20}
            color="#666"
            style={styles.infoIcon}
          />
          <Text style={styles.infoLabel}>반납 예정일</Text>
          <Text style={styles.infoValue}>{formatDate(item.dueDate)}</Text>
        </View>

        {item.returnDate && (
          <View style={styles.infoRow}>
            <MaterialCommunityIcons
              name="calendar-check"
              size={20}
              color="#666"
              style={styles.infoIcon}
            />
            <Text style={styles.infoLabel}>실제 반납일</Text>
            <Text style={styles.infoValue}>{formatDate(item.returnDate)}</Text>
          </View>
        )}
      </View>
    ),
    []
  );

  const renderListFooter = () => {
    if (isLoadingMore) {
      return (
        <View style={styles.listFooter}>
          <ActivityIndicator animating={true} size="small" />
        </View>
      );
    }

    if (!hasNext && agreementHistory.length > 0) {
      return (
        <View style={styles.listFooter}>
          <Text>마지막 기록입니다.</Text>
        </View>
      );
    }

    return null;
  };

  if (isLoading) {
    return (
      <View style={styles.centeredView}>
        <ActivityIndicator animating={true} size="large" />
      </View>
    );
  }

  if (!item) {
    return (
      <View style={styles.centeredView}>
        <Text>물품 정보를 찾을 수 없습니다.</Text>
        <Button mode="contained" onPress={() => navigation.goBack()}>
          뒤로가기
        </Button>
      </View>
    );
  }

  return (
    <SafeAreaView style={styles.container}>
      <FlatList
        data={agreementHistory}
        renderItem={renderAgreementItem}
        keyExtractor={(item) => item.id.toString()}
        onEndReached={fetchMoreAgreementHistory}
        onEndReachedThreshold={0.5}
        ListHeaderComponent={<ItemDetailsHeader item={item} styles={styles} />}
        ListFooterComponent={renderListFooter()}
      />
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#fff",
  },
  statsContainer: {
    flexDirection: "row",
    justifyContent: "space-between",
    paddingHorizontal: 12,
    marginTop: -40,
    marginBottom: 20,
    zIndex: 1,
  },
  centeredView: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
  },
  headerImage: {
    width: width,
    height: width * 0.75,
    backgroundColor: "#742020ff",
  },
  contentSection: {
    padding: 16,
  },
  titleSection: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 10,
    marginTop: 10,
  },
  title: {
    fontSize: 28,
    fontWeight: "bold",
    color: "#333",
    flexShrink: 1,
  },
  descriptionText: {
    color: "#666",
    lineHeight: 22,
  },
  statusBadge: {
    borderRadius: 6,
    paddingHorizontal: 10,
    paddingVertical: 4,
    minWidth: 70,
    height: 30,
    justifyContent: "center",
    alignItems: "center",
    marginLeft: 10,
  },
  statusBadgeText: {
    fontSize: 12,
    fontWeight: "bold",
    color: "#fff",
  },
  infoGroup: {
    marginVertical: 10,
  },
  sectionTitle: {
    fontWeight: "bold",
    marginBottom: 20,
    fontSize: 18,
    color: "#333",
  },
  infoRow: {
    flexDirection: "row",
    alignItems: "center",
    marginBottom: 8,
  },
  infoIcon: {
    marginRight: 10,
  },
  infoLabel: {
    fontSize: 13,
    color: "#666",
    minWidth: 80,
  },
  infoValue: {
    fontSize: 13,
    color: "#444",
    flex: 1,
  },
  divider: {
    marginVertical: 15,
  },
  agreementItemContainer: {
    padding: 16,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: "#ccc",
  },
  agreementHeaderRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 10,
  },
  textBlock: {
    flex: 1,
    marginRight: 8,
  },
  agreementItemTitle: {
    fontWeight: "bold",
    fontSize: 16,
    color: "#222",
    flexShrink: 1,
  },
  agreementItemDaysInline: {
    fontSize: 13,
    color: "#666",
    marginTop: 2,
  },
  agreementItemTerms: {
    fontSize: 13,
    color: "#999",
    marginBottom: 20,
  },
  agreementItemDate: {
    fontSize: 14,
    color: "#666",
  },
  agreementItemDays: {
    fontSize: 14,
    color: "#999",
    marginTop: 4,
    marginBottom: 10,
  },
  listFooter: {
    padding: 20,
  },
});
export default MyItemDetailScreen;
