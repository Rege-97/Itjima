import { MaterialCommunityIcons } from "@expo/vector-icons";
import React from "react";
import { StyleSheet, View } from "react-native";
import { Text } from "react-native-paper";

function StatBox({
  title,
  count,
  tone,
}: {
  title: string;
  count: number;
  tone: "orange" | "blue";
}) {
  const colors =
    tone === "orange"
      ? { bg: "#FFF4EB", border: "#FFE3CF", text: "#E86A17", icon: "#E86A17" }
      : { bg: "#EDF5FF", border: "#D4E6FF", text: "#2E78F6", icon: "#2E78F6" };

  return (
    <View
      style={[
        styles.statBox,
        {
          backgroundColor: colors.bg,
          borderColor: colors.border,
          shadowColor: "#000",
          shadowOpacity: 0.06,
          shadowRadius: 6,
          shadowOffset: { width: 0, height: 3 },
          elevation: 2,
        },
      ]}
    >
      <View style={styles.statRow}>
        <View style={{ flex: 1 }}>
          <Text style={[styles.statTitle, { color: colors.text }]}>
            {title}
          </Text>
          <Text style={styles.statCount}>{count.toLocaleString()}개</Text>
        </View>
        <MaterialCommunityIcons
          name="cube-outline"
          size={28}
          color={colors.icon}
        />
      </View>
    </View>
  );
}

export default StatBox;

const styles = StyleSheet.create({
  statBox: {
    flex: 1,
    borderRadius: 14,
    borderWidth: 1,
    paddingVertical: 14,
    paddingHorizontal: 16,
    marginVertical: 10,
  },
  statRow: {
    flexDirection: "row",
    alignItems: "center",
  },
  statTitle: {
    fontSize: 13,
    marginBottom: 4,
  },
  statCount: {
    fontSize: 18,
    fontWeight: "700",
  },
});
