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

const StatusBadge = ({ status }: { status: string }) => {
  let backgroundColor = "#ccc";
  let label = status;

  switch (status) {
    case "PENDING":
      backgroundColor = "#5c36f4";
      label = "요청";
      break;
    case "ACCEPTED":
      backgroundColor = "#4CAF50";
      label = "대여중";
      break;
    case "REJECTED":
      backgroundColor = "#F44336";
      label = "거절됨";
      break;
    case "COMPLETED":
      backgroundColor = "#2196F3";
      label = "완료됨";
      break;
    case "CANCELED":
      backgroundColor = "#9E9E9E";
      label = "취소됨";
      break;
    case "OVERDUE":
      backgroundColor = "#FF9800";
      label = "연체됨";
      break;
    default:
      backgroundColor = "#ccc";
      label = status;
  }

  return (
    <View style={[styles.statusBadge, { backgroundColor }]}>
      <Text style={styles.statusBadgeText}>{label}</Text>
    </View>
  );
};

const RoleBadge = ({ role }: { role: string }) => {
  let backgroundColor = "#ccc";
  let label = role;

  switch (role) {
    case "DEBTOR":
      backgroundColor = "#5c36f4";
      label = "빌려줌";
      break;
    case "CREDITOR":
      backgroundColor = "#4CAF50";
      label = "빌림";
      break;
  }

  return (
    <View style={[styles.roleBadge, { backgroundColor }]}>
      <Text style={styles.statusBadgeText}>{label}</Text>
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

  return (
    <TouchableOpacity
      onPress={() =>
        navigation.navigate("MyAgreementDetail", { agreementId: agreement.id })
      }
    >
      <View style={styles.listagreement}>
        <View style={styles.headerRow}>
          <View style={styles.thumbnailWrapper}>
            <Image
              source={{
                uri: agreement?.itemFileUrl
                  ? IMG_BASE_URL + agreement.itemFileUrl
                  : "https://via.placeholder.com/150",
              }}
              style={styles.squareAvatar}
            />
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
              <>
                <Text style={styles.amount}>
                  {agreement.amount}원{" "}
                  <Text style={styles.remaining}>
                    (잔금 {agreement.remainingAmount}원)
                  </Text>
                </Text>
              </>
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
                    반납일:{" "}
                    {agreement.returnDate
                      ? formatDate(agreement.returnDate)
                      : "없음"}
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
    borderRadius: 8,
    backgroundColor: "#eee",
    marginRight: 20,
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
    fontWeight: "600",
    color: "#d32f2f",
    marginTop: 2,
  },
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
    borderRadius: 6,
    paddingHorizontal: 8,
    paddingVertical: 4,
    justifyContent: "center",
    alignItems: "center",
  },
  roleBadge: {
    borderRadius: 6,
    paddingHorizontal: 8,
    paddingVertical: 4,
    marginLeft: 4,
    justifyContent: "center",
    alignItems: "center",
  },
  statusBadgeText: {
    fontSize: 11,
    fontWeight: "bold",
    color: "#fff",
  },
});

export default React.memo(AgreementCard);
