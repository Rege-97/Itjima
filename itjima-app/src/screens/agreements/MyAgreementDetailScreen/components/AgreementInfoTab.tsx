import React from "react";
import { Image, Linking, StyleSheet, View } from "react-native";
import { Text } from "react-native-paper";
import { MaterialCommunityIcons } from "@expo/vector-icons";
import { IMG_BASE_URL } from "@env";
import { formatDate } from "../hooks/useAgreementDetail";

const resolveImageUrl = (path?: string | null) => {
  if (!path) return "https://via.placeholder.com/800x600";
  return (IMG_BASE_URL || "") + path;
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
      backgroundColor = "#FFC107";
      label = "승인 대기 중";
      break;
    case "ACCEPTED":
      backgroundColor = "#2196F3";
      label = "대여중";
      break;
    case "REJECTED":
      backgroundColor = "#F44336";
      label = "거절됨";
      break;
    case "COMPLETED":
      backgroundColor = overdueReturn ? "#FF5722" : "#4CAF50";
      label = overdueReturn ? "연체반납" : "완료됨";
      break;
    case "CANCELED":
      backgroundColor = "#9E9E9E";
      label = "취소됨";
      break;
    case "OVERDUE":
      backgroundColor = "#E91E63";
      label = "연체됨";
      break;
  }
  return (
    <View style={[styles.badge, { backgroundColor }]}>
      <Text style={styles.badgeText}>{label}</Text>
    </View>
  );
};

const RoleBadge = ({ myRole }: { myRole: "DEBTOR" | "CREDITOR" }) => {
  const isDebtor = myRole === "DEBTOR";
  const backgroundColor = isDebtor ? "#4CAF50" : "#5c36f4";
  const text = isDebtor ? "빌림" : "빌려줌";
  return (
    <View style={[styles.badge, { backgroundColor, marginRight: 8 }]}>
      <Text style={styles.badgeText}>{text}</Text>
    </View>
  );
};

const InfoRow = ({
  icon,
  label,
  value,
  onPress,
}: {
  icon: any;
  label: string;
  value: string;
  onPress?: () => void;
}) => (
  <View style={styles.infoRow} onTouchEnd={onPress}>
    <MaterialCommunityIcons
      name={icon}
      size={20}
      color="#666"
      style={styles.infoIcon}
    />
    <Text style={styles.infoLabel}>{label}</Text>
    <Text style={styles.infoValue}>{value}</Text>
  </View>
);

const Section = ({
  title,
  children,
}: {
  title: string;
  children: React.ReactNode;
}) => (
  <View style={styles.section}>
    <Text style={styles.sectionTitle}>{title}</Text>
    <View style={styles.sectionBody}>{children}</View>
  </View>
);

export default function AgreementInfoTab({
  agreement,
  partnerName,
  partnerPhone,
  startAt,
  isItemLoan,
  isMoney,
  hasSettlement,
}: {
  agreement: any;
  partnerName: string;
  partnerPhone?: string;
  startAt: string | null;
  isItemLoan: boolean;
  isMoney: boolean;
  hasSettlement: boolean;
}) {
  return (
    <>
      <View style={styles.topBlock}>
        <View style={styles.titleRow}>
          <Text style={styles.partnerName}>{partnerName || "상대방"}</Text>
          <View style={styles.badgesRow}>
            <RoleBadge myRole={agreement.myRole} />
            <AgreementStatusBadge
              status={agreement.status}
              overdueReturn={!!agreement.isOverdueReturn}
            />
          </View>
        </View>

        {partnerPhone ? (
          <View style={{ marginTop: 10 }}>
            <InfoRow
              icon="phone"
              label="연락처"
              value={partnerPhone}
              onPress={() => Linking.openURL(`tel:${partnerPhone}`)}
            />
          </View>
        ) : null}

        {agreement.terms ? (
          <View style={{ marginTop: 6 }}>
            <InfoRow
              icon="file-document-outline"
              label="대여 상세"
              value={agreement.terms}
            />
          </View>
        ) : null}
      </View>

      <View style={styles.divider} />

      <Section title="대여 기간">
        <InfoRow
          icon="calendar-arrow-right"
          label="시작일"
          value={formatDate(startAt)}
        />
        <InfoRow
          icon="calendar-arrow-left"
          label="반납 예정일"
          value={formatDate(agreement.dueDate)}
        />
        {agreement.returnDate ? (
          <InfoRow
            icon="calendar-check"
            label="반납일"
            value={formatDate(agreement.returnDate)}
          />
        ) : null}
        {typeof agreement.rentalDays === "number" && (
          <InfoRow
            icon="timelapse"
            label="대여 기간"
            value={`${agreement.rentalDays}일`}
          />
        )}
      </Section>

      <View style={styles.divider} />

      {isItemLoan ? (
        <>
          <Section title="물품 정보">
            <Image
              source={{ uri: resolveImageUrl(agreement.itemFileUrl) }}
              style={styles.itemImage}
            />
            <InfoRow
              icon="cube-outline"
              label="물품명"
              value={agreement.itemTitle || "제목 없음"}
            />
            <InfoRow
              icon="text"
              label="물품 상세"
              value={agreement.itemDescription || "설명 없음"}
            />
          </Section>
          <View style={styles.divider} />
        </>
      ) : (
        <>
          <Section title="대출 정보">
            <InfoRow
              icon="cash-multiple"
              label="빌린 금액"
              value={
                typeof agreement.amount === "number"
                  ? `${agreement.amount.toLocaleString()} 원`
                  : "정보 없음"
              }
            />
            <InfoRow
              icon="cash-refund"
              label="남은 금액"
              value={
                typeof agreement.remainingAmount === "number"
                  ? `${agreement.remainingAmount.toLocaleString()} 원`
                  : "정보 없음"
              }
            />
          </Section>
          <View style={styles.divider} />
        </>
      )}

      {hasSettlement && isItemLoan && (
        <>
          <Section title="정산 정보">
            {typeof agreement.amount === "number" && (
              <InfoRow
                icon="cash-multiple"
                label="거래 금액"
                value={`${agreement.amount.toLocaleString()} 원`}
              />
            )}
            {typeof agreement.remainingAmount === "number" && (
              <InfoRow
                icon="cash-refund"
                label="남은 금액"
                value={`${agreement.remainingAmount.toLocaleString()} 원`}
              />
            )}
          </Section>
          <View style={styles.divider} />
        </>
      )}
    </>
  );
}

const styles = StyleSheet.create({
  topBlock: {
    paddingHorizontal: 16,
    paddingTop: 20,
    paddingBottom: 14,
  },
  titleRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  partnerName: {
    fontSize: 22,
    fontWeight: "bold",
    color: "#222",
    flexShrink: 1,
  },
  badgesRow: {
    flexDirection: "row",
    alignItems: "center",
  },
  badge: {
    borderRadius: 6,
    paddingHorizontal: 10,
    paddingVertical: 4,
    minWidth: 70,
    height: 30,
    justifyContent: "center",
    alignItems: "center",
  },
  badgeText: {
    fontSize: 12,
    fontWeight: "bold",
    color: "#fff",
  },

  section: {
    paddingHorizontal: 16,
    paddingVertical: 12,
  },
  sectionTitle: {
    fontWeight: "bold",
    marginBottom: 16,
    fontSize: 18,
    color: "#333",
  },
  sectionBody: {
    borderTopWidth: StyleSheet.hairlineWidth,
    borderTopColor: "transparent",
  },

  divider: {
    height: 10,
    backgroundColor: "#f4f5f7",
  },

  infoRow: {
    flexDirection: "row",
    alignItems: "center",
    paddingVertical: 10,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: "#e6e6e6",
  },
  infoIcon: {
    marginRight: 10,
  },
  infoLabel: {
    fontSize: 13,
    color: "#666",
    minWidth: 90,
  },
  infoValue: {
    fontSize: 13,
    color: "#333",
    flex: 1,
  },

  itemImage: {
    width: "100%",
    aspectRatio: 4 / 3,
    borderRadius: 10,
    backgroundColor: "#eee",
    marginBottom: 12,
  },
});
