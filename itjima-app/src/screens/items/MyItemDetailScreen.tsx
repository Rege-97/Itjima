import React, { useEffect, useState } from "react";
import {
  Alert,
  Dimensions,
  Image,
  SafeAreaView,
  ScrollView,
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
import { getItemDetailApi } from "../../api/items";
import { MaterialCommunityIcons } from "@expo/vector-icons";
import StatisticsCard from "./components/StatisticsCard";

const { width } = Dimensions.get("window");

const formatDate = (dateString: string | null) => {
  if (!dateString) return "정보 없음";
  const date = new Date(dateString);
  return date.toLocaleString("ko-KR", {
    year: "numeric",
    month: "long",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
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

const MyItemDetailScreen = ({ route, navigation }: any) => {
  const { itemId } = route.params;
  const [item, setItem] = useState<any>(null);
  const [isLoading, setIsLoading] = useState(true);

  const fetchItemDetails = async (itemId: number) => {
    setIsLoading(true);
    try {
      const response = await getItemDetailApi(itemId);
      setItem(response.data.data);
    } catch (error) {
      console.error("물품을 불러오는데 실패했습니다:", error);
      Alert.alert("오류", "물품 정보를 불러올 수 없습니다.");
      navigation.goBack();
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (itemId !== undefined && itemId !== null) {
      fetchItemDetails(itemId);
    } else {
      Alert.alert("오류", "유효하지 않은 물품 정보입니다.");
      navigation.goBack();
    }
  }, []);

  if (isLoading) {
    return (
      <View style={{ flex: 1, justifyContent: "center", alignItems: "center" }}>
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
      <ScrollView showsVerticalScrollIndicator={false}>
        <Image
          source={{
            uri:
              item.fileUrl ||
              "https://via.placeholder.com/400x300?text=No+Image",
          }}
          style={styles.headerImage}
        />
        <View style={styles.statsContainer}>
          <StatisticsCard
            title="총 대여"
            value={`${item.rentalCount}회`}
            iconName="account-group"
            iconColor="#4285F4" // 구글 파란색
          />
          <StatisticsCard
            title="총 대여일"
            value={`${item.totalRentalDays}일`}
            iconName="calendar-multiple"
            iconColor="#34A853" // 구글 초록색
          />
          <StatisticsCard
            title="평균 기간"
            value={`${item.avgRentalDays}일`}
            iconName="chart-line"
            iconColor="#EA4335" // 구글 빨간색
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

          {/* 대여 정보가 있을 경우에만 표시 */}
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
                    <Text style={styles.infoValue}>
                      {item.currentDebtorName}
                    </Text>
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
        </View>
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#fff",
  },
  statsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
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
});
export default MyItemDetailScreen;
