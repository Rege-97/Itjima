import React from "react";
import { FlatList, StyleSheet, View } from "react-native";
import { ActivityIndicator, Text } from "react-native-paper";
import { MaterialCommunityIcons } from "@expo/vector-icons";

const actionMessageMap: Record<string, string> = {
  AGREEMENT_CREATE: "대여 요청 생성",
  AGREEMENT_ACCEPT: "대여 수락",
  AGREEMENT_REJECT: "대여 거절",
  AGREEMENT_CANCEL: "대여 취소",
  AGREEMENT_COMPLETE: "대여 완료",
  TRANSACTION_CREATE: "상환 요청 생성",
  TRANSACTION_CONFIRM: "상환 승인",
  TRANSACTION_REJECT: "상환 거절",
};

const ActivityLogItem = ({ userName, action, createdAt }: any) => {
  const actionMsg = action ? actionMessageMap[action] || action : "알 수 없음";
  return (
    <View style={styles.logItem}>
      <MaterialCommunityIcons
        name="history"
        size={18}
        color="#666"
        style={{ marginRight: 8 }}
      />
      <View style={{ flex: 1 }}>
        <Text style={styles.logMessage}>
          [{userName}] {actionMsg}
        </Text>
        {/* 원본처럼 createdAt 그대로 노출 */}
        <Text style={styles.logDate}>{createdAt}</Text>
      </View>
    </View>
  );
};

export default function ActivityLogTab({
  items,
  isLoading,
  hasNext,
  onEndReached,
}: {
  items: any[];
  isLoading: boolean;
  hasNext: boolean;
  onEndReached: () => void;
}) {
  return (
    <View style={styles.section}>
      <Text style={styles.sectionTitle}>활동 로그</Text>
      <FlatList
        data={items}
        keyExtractor={(item) => String(item.id ?? item.createdAt)}
        renderItem={({ item }) => <ActivityLogItem {...item} />}
        ListFooterComponent={
          isLoading ? (
            <View style={{ paddingVertical: 12 }}>
              <ActivityIndicator animating />
            </View>
          ) : !hasNext && items.length > 0 ? (
            <Text style={styles.endText}>마지막 로그입니다.</Text>
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
  logDate: {
    fontSize: 12,
    color: "#888",
    marginTop: 2,
  },
  endText: {
    fontSize: 12,
    color: "#999",
    textAlign: "center",
    marginTop: 8,
  },
});
