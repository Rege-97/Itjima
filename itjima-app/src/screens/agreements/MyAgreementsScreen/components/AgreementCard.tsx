import { IMG_BASE_URL } from "@env";
import { MaterialCommunityIcons } from "@expo/vector-icons";
import React from "react";
import { Image, StyleSheet, TouchableOpacity, View } from "react-native";
import { Divider, Text } from "react-native-paper";

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

const formatKRW = (n?: number | string) => {
  if (n == null) return "-";
  const num = typeof n === "string" ? Number(n) : n;
  if (Number.isNaN(num)) return "-";
  return num.toLocaleString("ko-KR");
};

const StatusBadge = ({ status }: { status: string }) => {
  const map: Record<string, { bg: string; label: string }> = {
    PENDING: { bg: "#5c36f4", label: "요청" },
    ACCEPTED: { bg: "#4CAF50", label: "대여중" },
    REJECTED: { bg: "#F44336", label: "거절됨" },
    COMPLETED: { bg: "#2196F3", label: "완료됨" },
    CANCELED: { bg: "#9E9E9E", label: "취소됨" },
    OVERDUE: { bg: "#FF9800", label: "연체됨" },
  };
  const tone = map[status] || { bg: "#9E9E9E", label: status };

  return (
    <View style={[styles.statusBadge, { backgroundColor: tone.bg }]}>
      <Text style={styles.statusBadgeText}>{tone.label}</Text>
    </View>
  );
};

const RoleBadge = ({ role }: { role: string }) => {
  const map: Record<string, { bg: string; label: string }> = {
    DEBTOR: { bg: "#009688", label: "빌려줌" },
    CREDITOR: { bg: "#E91E63", label: "빌림" },
  };
  const tone = map[role] || { bg: "#666", label: role };

  return (
    <View style={[styles.roleBadge, { backgroundColor: tone.bg }]}>
      <Text style={styles.statusBadgeText}>{tone.label}</Text>
    </View>
  );
};

const AgreementCard = ({
  agreement,
  navigation,
}: {
  agreement: any;
  navigation: any;
}) => {
  const isMoneyLoan = agreement.itemType === "MONEY";
  const fileUrl = agreement?.itemFileUrl
    ? IMG_BASE_URL + agreement.itemFileUrl
    : null;

  const Thumb = () => {
    if (isMoneyLoan) {
      return (
        <View style={[styles.squareAvatar, styles.moneyThumb]}>
          <MaterialCommunityIcons
            name="cash-multiple"
            size={34}
            color="#7C3AED"
          />
          <Text style={styles.moneyThumbText}>금전</Text>
        </View>
      );
    }
    if (fileUrl) {
      return <Image source={{ uri: fileUrl }} style={styles.squareAvatar} />;
    }
    return (
      <View style={[styles.squareAvatar, styles.thumbPlaceholder]}>
        <MaterialCommunityIcons
          name="image-off-outline"
          size={28}
          color="#9CA3AF"
        />
        <Text style={styles.placeholderText}>이미지 없음</Text>
      </View>
    );
  };

  return (
    <TouchableOpacity
      onPress={() =>
        navigation.navigate("MyAgreementDetail", { agreementId: agreement.id })
      }
    >
      <View style={styles.listagreement}>
        <View style={styles.headerRow}>
          <View style={styles.thumbnailWrapper}>
            <Thumb />
            <View style={styles.statusOverlay}>
              <StatusBadge status={agreement.status} />
            </View>
          </View>

          <View style={styles.rightCol}>
            <View style={styles.titleRow}>
              <Text style={styles.title} numberOfLines={1}>
                {agreement.partnerName}
              </Text>
              <RoleBadge role={agreement.partnerRole} />
            </View>

            <Text style={styles.itemTitle} numberOfLines={1}>
              {agreement.itemTitle}
            </Text>

            {isMoneyLoan && (
              <Text style={styles.amount}>
                {formatKRW(agreement.amount)}원{" "}
                <Text style={styles.remaining}>
                  (잔금 {formatKRW(agreement.remainingAmount)}원)
                </Text>
              </Text>
            )}

            <View style={styles.dateCol}>
              <View style={styles.statagreement}>
                <MaterialCommunityIcons
                  name="calendar"
                  size={14}
                  color="#555"
                />
                <Text style={styles.statText}>
                  반납예정일: {formatDate(agreement.dueAt)}
                </Text>
              </View>
              {agreement.returnDate && (
                <View style={styles.statagreement}>
                  <MaterialCommunityIcons
                    name="calendar-check"
                    size={14}
                    color="#555"
                  />
                  <Text style={styles.statText}>
                    반납일: {formatDate(agreement.returnDate)}
                  </Text>
                </View>
              )}
            </View>
          </View>
        </View>
        <Divider style={styles.divider} />
      </View>
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  listagreement: {
    paddingVertical: 8,
    paddingHorizontal: 4,
    backgroundColor: "#fff",
  },
  headerRow: {
    flexDirection: "row",
    alignItems: "flex-start",
  },
  thumbnailWrapper: {
    position: "relative",
  },
  squareAvatar: {
    width: 120,
    height: 120,
    borderRadius: 10,
    backgroundColor: "#eee",
    marginRight: 20,
    overflow: "hidden",
    alignItems: "center",
    justifyContent: "center",
  },
  moneyThumb: {
    backgroundColor: "#F5F3FF", // light violet
    borderWidth: 1,
    borderColor: "#EDE9FE", // violet border
  },
  moneyThumbText: {
    marginTop: 6,
    fontSize: 12,
    fontWeight: "700",
    color: "#7C3AED", // violet text
  },
  thumbPlaceholder: {
    backgroundColor: "#F3F4F6",
  },
  placeholderText: {
    marginTop: 6,
    fontSize: 11,
    color: "#9CA3AF",
  },
  statusOverlay: {
    position: "absolute",
    top: 8,
    left: 8,
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
    marginBottom: 4,
    flexWrap: "wrap",
  },
  title: {
    fontSize: 15,
    fontWeight: "bold",
    marginRight: 6,
  },
  itemTitle: {
    fontSize: 12,
    color: "#333",
    marginTop: 2,
  },
  amount: {
    fontSize: 13,
    fontWeight: "600",
    color: "#444",
    marginTop: 6,
  },
  remaining: {
    fontSize: 13,
    fontWeight: "700",
    color: "#d32f2f",
    marginTop: 2,
  }, // 빨강 복원
  dateCol: {
    flexDirection: "column",
    alignItems: "flex-start",
    marginTop: "auto",
  },
  statagreement: {
    flexDirection: "row",
    alignItems: "center",
    marginBottom: 2,
  },
  statText: {
    fontSize: 12,
    color: "#777",
    marginLeft: 4,
  },
  divider: {
    marginTop: 12,
  },
  statusBadge: {
    borderRadius: 999,
    paddingHorizontal: 8,
    paddingVertical: 4,
    justifyContent: "center",
    alignItems: "center",
  },
  statusBadgeText: {
    fontSize: 11,
    fontWeight: "700",
    color: "#fff",
  },
  roleBadge: {
    borderRadius: 999,
    paddingHorizontal: 8,
    paddingVertical: 4,
    marginLeft: 6,
    justifyContent: "center",
    alignItems: "center",
  },
});

export default React.memo(AgreementCard);
