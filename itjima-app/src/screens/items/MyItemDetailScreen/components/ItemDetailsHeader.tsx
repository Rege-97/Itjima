import { MaterialCommunityIcons } from "@expo/vector-icons";
import React from "react";
import { Dimensions, Image, StyleSheet, View } from "react-native";
import { Divider, Text } from "react-native-paper";
import StatisticsCard from "../../components/StatisticsCard";

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

export const ItemDetailsHeader = ({ item }: { item: any }) => {
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

const styles = StyleSheet.create({
  statsContainer: {
    flexDirection: "row",
    justifyContent: "space-between",
    paddingHorizontal: 12,
    marginTop: -40,
    marginBottom: 20,
    zIndex: 1,
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
});
