import React from "react";
import { StyleSheet, TouchableOpacity, View } from "react-native";
import { Text } from "react-native-paper";

interface SummaryBoxProps {
  counts: {
    itemAllCount?: number;
    itemLoanCount?: number;
    itemAvailableCount?: number;
  };
  activeFilter: string | null;
  onFilterPress: (status: string | null) => void;
}

const SummaryBox = ({
  counts,
  activeFilter,
  onFilterPress,
}: SummaryBoxProps) => {
  const total = counts.itemAllCount || 0;
  const onLoan = counts.itemLoanCount || 0;
  const available = counts.itemAvailableCount || 0;

  return (
    <View style={styles.summaryContainer}>
      <TouchableOpacity
        onPress={() => onFilterPress(null)}
        style={styles.summaryItem}
      >
        <Text style={styles.summaryLabel}>총 물건</Text>
        <Text
          style={[
            styles.summaryValue,
            activeFilter === null && styles.activeFilter,
          ]}
        >
          {total}개
        </Text>
      </TouchableOpacity>
      <TouchableOpacity
        onPress={() => onFilterPress("ON_LOAN")}
        style={styles.summaryItem}
      >
        <Text style={styles.summaryLabel}>대여중</Text>
        <Text
          style={[
            styles.summaryValue,
            { color: "#F44336" },
            activeFilter === "ON_LOAN" && styles.activeFilter,
          ]}
        >
          {onLoan}개
        </Text>
      </TouchableOpacity>
      <TouchableOpacity
        onPress={() => onFilterPress("AVAILABLE")}
        style={styles.summaryItem}
      >
        <Text style={styles.summaryLabel}>대여가능</Text>
        <Text
          style={[
            styles.summaryValue,
            { color: "#4CAF50" },
            activeFilter === "AVAILABLE" && styles.activeFilter,
          ]}
        >
          {available}개
        </Text>
      </TouchableOpacity>
    </View>
  );
};

const styles = StyleSheet.create({
    
  summaryContainer: {
    flexDirection: "row",
    justifyContent: "space-around",
    padding: 16,
    backgroundColor: "#f7f7f7",
    borderBottomWidth: 1,
    borderBottomColor: "#eee",
    marginBottom: 8,
  },
  summaryItem: {
    alignItems: "center",
  },
  summaryLabel: {
    fontSize: 14,
    color: "#555",
    marginBottom: 4,
  },
  summaryValue: {
    fontSize: 18,
    fontWeight: "bold",
  },
  activeFilter: {
    textDecorationLine: "underline",
  },
})

export default SummaryBox