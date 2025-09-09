import React from "react";
import { StyleSheet, View } from "react-native";
import { Button } from "react-native-paper";

export default function ActionButtons({
  myRole,
  status,
  isMoney,
  onAccept,
  onReject,
  onCancel,
  onComplete,
  onExtendPress,
  onRepayPress,
}: {
  myRole: "DEBTOR" | "CREDITOR";
  status:
    | "PENDING"
    | "ACCEPTED"
    | "REJECTED"
    | "COMPLETED"
    | "CANCELED"
    | "OVERDUE";
  isMoney: boolean;
  onAccept: () => void;
  onReject: () => void;
  onCancel: () => void;
  onComplete: () => void;
  onExtendPress: () => void;
  onRepayPress: () => void;
}) {
  return (
    <View style={styles.actions}>
      {myRole === "DEBTOR" && status === "PENDING" && (
        <View style={styles.row}>
          <Button mode="contained" style={styles.button} onPress={onAccept}>
            대여 승낙
          </Button>
          <Button mode="outlined" style={styles.button} onPress={onReject}>
            대여 거절
          </Button>
        </View>
      )}

      {myRole === "DEBTOR" && status === "ACCEPTED" && isMoney && (
        <Button mode="contained" style={styles.button} onPress={onRepayPress}>
          상환 요청
        </Button>
      )}

      {myRole === "CREDITOR" && status === "PENDING" && (
        <Button mode="outlined" style={styles.button} onPress={onCancel}>
          대여 취소
        </Button>
      )}

      {myRole === "CREDITOR" &&
        (status === "ACCEPTED" || status === "OVERDUE") && (
          <Button mode="contained" style={styles.button} onPress={onComplete}>
            대여 완료
          </Button>
        )}

      {myRole === "CREDITOR" && status !== "COMPLETED" && (
        <Button mode="outlined" style={styles.button} onPress={onExtendPress}>
          대여기간 연장
        </Button>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  actions: {
    paddingHorizontal: 16,
    paddingVertical: 12,
  },
  row: {
    flexDirection: "row",
    justifyContent: "space-between",
  },
  button: {
    flex: 1,
    marginHorizontal: 4,
    marginVertical: 6,
  },
});
