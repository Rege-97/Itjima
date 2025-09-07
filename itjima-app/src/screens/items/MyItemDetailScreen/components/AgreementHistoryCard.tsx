import { MaterialCommunityIcons } from "@expo/vector-icons";
import React from "react";
import { StyleSheet, View } from "react-native";
import { Text } from "react-native-paper";

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

export const AgreementHistoryCard = ({ item }: { item: any }) => {
  return (
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
  );
};

const styles = StyleSheet.create({
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
})