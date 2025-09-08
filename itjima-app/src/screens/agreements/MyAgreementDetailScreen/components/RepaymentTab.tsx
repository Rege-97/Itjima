import React from "react";
import { FlatList, StyleSheet, View } from "react-native";
import { ActivityIndicator, Button, Text } from "react-native-paper";
import { MaterialCommunityIcons } from "@expo/vector-icons";
import { formatDate } from "../hooks/useAgreementDetail";

const repaymentStatusMap: Record<string, string> = {
  PENDING: "대기중",
  CONFIRMED: "승인됨",
  REJECTED: "거절됨",
};

const RepaymentItem = ({
  item,
  canAct,
  acting,
  onConfirm,
  onReject,
}: {
  item: any;
  canAct: boolean;
  acting: boolean;
  onConfirm: (id: number) => void;
  onReject: (id: number) => void;
}) => {
  const statusLabel = item.status
    ? repaymentStatusMap[item.status] || item.status
    : "";
  const amountStr =
    typeof item.amount === "number"
      ? `${item.amount.toLocaleString()} 원`
      : item.amount
      ? `${item.amount} 원`
      : "금액 미상";

  return (
    <View style={styles.logItem}>
      <MaterialCommunityIcons
        name="cash-check"
        size={18}
        color="#666"
        style={{ marginRight: 8 }}
      />
      <View style={{ flex: 1 }}>
        <Text style={styles.logMessage}>{amountStr}</Text>
        {!!item.memo && <Text style={styles.logSub}>{item.memo}</Text>}
        <View style={styles.repayBottomRow}>
          <Text style={styles.logDate}>
            {formatDate(item.createdAt || item.created_at || null)}{" "}
            {statusLabel && `(${statusLabel})`}
          </Text>

          {canAct && item.status === "PENDING" && (
            <View style={styles.repayActionsRow}>
              <Button
                mode="contained"
                compact
                style={styles.repayActionBtn}
                disabled={acting}
                onPress={() => onConfirm(item.id)}
              >
                승인
              </Button>
              <Button
                mode="outlined"
                compact
                style={styles.repayActionBtn}
                disabled={acting}
                onPress={() => onReject(item.id)}
              >
                거절
              </Button>
            </View>
          )}
        </View>
      </View>
    </View>
  );
};

export default function RepaymentTab({
  remainingAmount,
  items,
  isLoading,
  hasNext,
  onEndReached,
  canAct,
  actingIds,
  onConfirm,
  onReject,
}: {
  remainingAmount?: number;
  items: any[];
  isLoading: boolean;
  hasNext: boolean;
  onEndReached: () => void;
  canAct: boolean;
  actingIds: Set<number>;
  onConfirm: (id: number) => void;
  onReject: (id: number) => void;
}) {
  return (
    <View style={styles.section}>
      <Text style={styles.sectionTitle}>상환 기록</Text>

      {typeof remainingAmount === "number" && (
        <View style={styles.infoRow}>
          <MaterialCommunityIcons
            name="cash-refund"
            size={20}
            color="#666"
            style={{ marginRight: 10 }}
          />
          <Text style={styles.infoLabel}>남은 금액</Text>
          <Text style={styles.infoValue}>
            {remainingAmount.toLocaleString()} 원
          </Text>
        </View>
      )}
      <View style={{ height: 6 }} />

      <FlatList
        data={items}
        keyExtractor={(item) => String(item.id ?? item.createdAt)}
        renderItem={({ item }) => (
          <RepaymentItem
            item={item}
            canAct={canAct}
            acting={actingIds.has(item.id)}
            onConfirm={onConfirm}
            onReject={onReject}
          />
        )}
        ListEmptyComponent={
          !isLoading ? (
            <Text style={styles.emptyText}>상환 기록이 없습니다.</Text>
          ) : null
        }
        ListFooterComponent={
          isLoading ? (
            <View style={{ paddingVertical: 12 }}>
              <ActivityIndicator animating />
            </View>
          ) : !hasNext && items.length > 0 ? (
            <Text style={styles.endText}>마지막 상환 기록입니다.</Text>
          ) : null
        }
        onEndReachedThreshold={0.4}
        onEndReached={() => {
          if (hasNext && !isLoading) onEndReached();
        }}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  section: {
    paddingHorizontal: 16,
    paddingVertical: 12,
    flex: 1,
    backgroundColor: "#fff",
  },
  sectionTitle: {
    fontWeight: "bold",
    marginBottom: 16,
    fontSize: 18,
    color: "#333",
  },

  infoRow: {
    flexDirection: "row",
    alignItems: "center",
    paddingVertical: 10,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: "#e6e6e6",
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

  logItem: {
    flexDirection: "row",
    alignItems: "flex-start",
    paddingVertical: 15,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: "#e6e6e6",
  },
  logMessage: {
    fontSize: 16,
    color: "#333",
  },
  logSub: {
    fontSize: 12,
    color: "#666",
    marginTop: 4,
  },
  logDate: {
    fontSize: 12,
    color: "#888",
    marginTop: 2,
  },

  repayBottomRow: {
    marginTop: 4,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
  },
  repayActionsRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: 8,
  },
  repayActionBtn: {
    marginLeft: 6,
    minWidth: 64,
    height: 32,
    justifyContent: "center",
  },

  emptyText: {
    fontSize: 13,
    color: "#777",
  },
  endText: {
    fontSize: 12,
    color: "#999",
    textAlign: "center",
    marginTop: 8,
  },
});
