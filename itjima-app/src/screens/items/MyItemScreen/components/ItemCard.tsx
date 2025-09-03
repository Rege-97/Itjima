import { MaterialCommunityIcons } from "@expo/vector-icons";
import React from "react";
import { Image, StyleSheet, TouchableOpacity, View } from "react-native";
import { Divider, Text } from "react-native-paper";
import { SafeAreaView } from "react-native-safe-area-context";

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

  const ItemCard = ({ item,navigation }: { item: any, navigation: any }) => {
  return (
    <TouchableOpacity onPress={() => navigation.navigate("MyItemDetail", { itemId: item.id })}>
    <View style={styles.listItem}>
      <View style={styles.headerRow}>
        <Image
          source={{ uri: item.fileUrl || "https://via.placeholder.com/150" }}
          style={styles.squareAvatar}
        />
        <View style={styles.rightCol}>
          <View style={styles.titleRow}>
            <Text style={styles.title} numberOfLines={1}>{item.title}</Text>
            <StatusBadge status={item.status} />
          </View>
          <Text style={styles.subtitle} numberOfLines={2}>{item.description}</Text>
          <View style={styles.statsRow}>
            <View style={styles.statItem}>
              <MaterialCommunityIcons name="account-supervisor-outline" size={15} color="#555" />
              <Text style={styles.statText}>{item.loanCount}회 대여</Text>
            </View>
            <Text style={styles.lastLoanText}>
              마지막 대여: {item.lastDebtorName || "없음"}
            </Text>
          </View>
        </View>
      </View>
      <Divider style={styles.divider} />
    </View>
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
    listItem: {
    paddingVertical: 8,
    paddingHorizontal: 4,
    backgroundColor: "#fff",
  },

  headerRow: {
    flexDirection: "row",
    alignItems: "flex-start",
  },
  squareAvatar: {
    width: 120,
    height: 120,
    borderRadius: 8,
    backgroundColor: "#eee",
    marginRight: 20,
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
    flexWrap: "wrap",
  },
  title: {
    fontSize: 16,
    fontWeight: "bold",
    marginRight: 8,
  },
  subtitle: {
    fontSize: 12,
    color: "#7c7c7c",
    marginTop: 6,
    marginBottom: 40,
    marginLeft: 1,
  },

  statsRow: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    marginTop: 8,
    paddingRight: 8,
  },
  statItem: {
    flexDirection: "row",
    alignItems: "center",
  },
  statText: {
    marginLeft: 4,
    color: "#747474",
    fontSize: 12,
  },
  lastLoanText: {
    color: "#747474",
    fontSize: 12,
  },

  divider: {
    marginTop: 14,
  },

  statusBadge: {
    borderRadius: 6,
    paddingHorizontal: 8,
    paddingVertical: 4,
    width: 60,
    height: 24,
    justifyContent: "center",
    alignItems: "center",
  },
  statusBadgeText: {
    fontSize: 12,
    fontWeight: "bold",
    color: "#fff",
  },

})

export default React.memo(ItemCard);