import { MaterialCommunityIcons } from "@expo/vector-icons";
import React from "react";
import { StyleSheet, View } from "react-native";
import { Text } from "react-native-paper";
import { PendingItem } from "../hooks/useHomeScreen";

const timeAgo = (iso?: string) => {
  if (!iso) return "";
  const diff = Date.now() - new Date(iso).getTime();
  const m = Math.floor(diff / 60000);
  if (m < 1) return "방금 전";
  if (m < 60) return `${m}분 전`;
  const h = Math.floor(m / 60);
  if (h < 24) return `${h}시간 전`;
  const d = Math.floor(h / 24);
  return `${d}일 전`;
};

function PendingRow({ item }: { item: PendingItem }) {
  const titleUser = item.pendingUser ? `[${item.pendingUser}] 님의 ` : "";
  const statusText = item.status ? item.status : "요청을 보냈습니다";

  return (
    <View style={styles.pendingRow}>
      <View style={{ flex: 1 }}>
        <Text numberOfLines={1} style={styles.pendingTitle}>
          {titleUser}
          {statusText}
        </Text>
        {item.description ? (
          <Text numberOfLines={1} style={styles.pendingSub}>
            {item.description}
          </Text>
        ) : null}
      </View>

      <View style={{ alignItems: "flex-end" }}>
        {!!item.createdAt && (
          <Text style={styles.pendingTime}>{timeAgo(item.createdAt)}</Text>
        )}
        <MaterialCommunityIcons
          name="chevron-right"
          size={20}
          color="#9CA3AF"
        />
      </View>
    </View>
  );
}
export default PendingRow;

const styles = StyleSheet.create({
  pendingRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: 12,
    padding: 14,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: "#F3F4F6",
    backgroundColor: "#FFFFFF",
  },
  pendingTitle: {
    fontWeight: "700",
    fontSize: 14,
    color: "#111827",
  },
  pendingSub: {
    color: "#6b7280",
    marginTop: 2,
    fontSize: 12,
  },
  pendingTime: {
    color: "#9CA3AF",
    fontSize: 12,
    marginBottom: 2,
  },
});
