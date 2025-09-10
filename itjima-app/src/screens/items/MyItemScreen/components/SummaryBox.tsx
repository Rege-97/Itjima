import React from "react";
import { StyleSheet, TouchableOpacity, View } from "react-native";
import { Text } from "react-native-paper";
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
const compactKR = (n: number) => {
  if (n >= 100000000) {
    return `${Math.round(n / 10000)}만`;
  }
  if (n >= 10000) {
    const v = n / 10000;
    const s = v >= 10 ? Math.round(v).toString() : v.toFixed(1);
    return `${s.replace(/\.0$/, "")}만`;
  }
  return n.toLocaleString("ko-KR");
};

const SummaryBox = ({ counts, activeFilter, onFilterPress }: SummaryBoxProps) => {
  const total = counts.itemAllCount ?? 0;
  const onLoan = counts.itemLoanCount ?? 0;
  const available = counts.itemAvailableCount ?? 0;

  return (
    <View style={styles.row}>
      <Tile
        label="총 물건"
        count={total}
        icon="cube-outline"
        tone={{ bg: "#F3F4F6", border: "#E5E7EB", icon: "#374151", text: "#111827", badgeBg: "#E5E7EB", badgeText: "#111827" }}
        active={activeFilter === null}
        onPress={() => onFilterPress(null)}
      />
      <Tile
        label="대여중"
        count={onLoan}
        icon="swap-horizontal"
        tone={{ bg: "#FEF3C7", border: "#FDE68A", icon: "#B45309", text: "#7C2D12", badgeBg: "#FDE68A", badgeText: "#7C2D12" }}
        active={activeFilter === "ON_LOAN"}
        onPress={() => onFilterPress("ON_LOAN")}
      />
      <Tile
        label="대여가능"
        count={available}
        icon="check-circle-outline"
        tone={{ bg: "#DCFCE7", border: "#BBF7D0", icon: "#065F46", text: "#065F46", badgeBg: "#BBF7D0", badgeText: "#065F46" }}
        active={activeFilter === "AVAILABLE"}
        onPress={() => onFilterPress("AVAILABLE")}
      />
    </View>
  );
};

function Tile({
  label,
  count,
  icon,
  tone,
  active,
  onPress,
}: {
  label: string;
  count: number;
  icon: any;
  tone: { bg: string; border: string; icon: string; text: string; badgeBg: string; badgeText: string };
  active: boolean;
  onPress: () => void;
}) {
  return (
    <TouchableOpacity
      activeOpacity={0.9}
      onPress={onPress}
      style={[
        styles.tile,
        { backgroundColor: tone.bg, borderColor: tone.border },
        active && styles.tileActive,
      ]}
    >
      <View style={styles.tileTop}>
        <MaterialCommunityIcons name={icon} size={18} color={tone.icon} />
        <Text style={[styles.label, { color: tone.text }]} numberOfLines={1}>
          {label}
        </Text>
      </View>

      <View style={[styles.badge, { backgroundColor: tone.badgeBg }]}>
        <Text style={[styles.badgeText, { color: tone.badgeText }]}>
          {compactKR(count)}
        </Text>
      </View>
    </TouchableOpacity>
  );
}

const TILE_HEIGHT = 74;

const styles = StyleSheet.create({
  row: {
    flexDirection: "row",
    gap: 12,
    paddingHorizontal: 16,
    marginBottom: 8,
  },
  tile: {
    flex: 1,
    height: TILE_HEIGHT,
    borderWidth: 1,
    borderRadius: 14,
    paddingVertical: 10,
    paddingHorizontal: 12,
    justifyContent: "space-between",
    shadowColor: "#000",
    shadowOpacity: 0.06,
    shadowRadius: 6,
    shadowOffset: { width: 0, height: 3 },
    elevation: 2,
  },
  tileActive: {
    borderColor: "#7C3AED",
    shadowOpacity: 0.12,
    elevation: 3,
  },
  tileTop: {
    flexDirection: "row",
    alignItems: "center",
    gap: 8,
    minWidth: 0,
  },
  label: {
    fontSize: 13,
    fontWeight: "700",
    flexShrink: 1,
  },
  badge: {
    alignSelf: "center",
    minWidth: 36,
    paddingHorizontal: 8,
    height: 24,
    borderRadius: 999,
    alignItems: "center",
    justifyContent: "center",
  },
  badgeText: {
    fontSize: 12,
    fontWeight: "800",
  },
});

export default SummaryBox;
