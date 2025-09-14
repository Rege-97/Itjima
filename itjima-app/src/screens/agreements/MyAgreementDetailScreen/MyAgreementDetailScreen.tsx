import React, { useCallback, useEffect, useState } from "react";
import {
  ScrollView,
  StyleSheet,
  View,
  TouchableOpacity,
  Alert,
} from "react-native";
import {
  ActivityIndicator,
  Appbar,
  Button,
  Dialog,
  Portal,
  Text,
  TextInput,
} from "react-native-paper";
import { DatePickerModal } from "react-native-paper-dates";
import { useFocusEffect } from "@react-navigation/native";
import { MaterialCommunityIcons } from "@expo/vector-icons";
import {
  agreementAccept,
  agreementCancel,
  agreementComplete,
  agreementExtend,
  agreementReject,
  agreementTransaction,
} from "../../../api/agreements";

import useAgreementDetail from "./hooks/useAgreementDetail";
import AgreementInfoTab from "./components/AgreementInfoTab";
import ActionButtons from "./components/ActionButtons";
import ActivityLogTab from "./components/ActivityLogTab";
import RepaymentTab from "./components/RepaymentTab";

type Tab = "INFO" | "LOG" | "REPAY";

const MyAgreementDetailScreen = ({ route, navigation }: any) => {
  const { agreementId } = route.params;

  const {
    agreement,
    isLoading,
    logItems,
    logHasNext,
    isLogLoading,
    fetchLogs,
    repayItems,
    repayHasNext,
    isRepayLoading,
    fetchRepayments,
    repayActingIds,
    handleConfirmRepay,
    handleRejectRepay,
    fetchInitialData,
  } = useAgreementDetail(agreementId, navigation);

  const [activeTab, setActiveTab] = useState<Tab>("INFO");
  const [repayVisible, setRepayVisible] = useState(false);
  const [repayAmount, setRepayAmount] = useState("");
  const [extendVisible, setExtendVisible] = useState(false);
  const [extendDate, setExtendDate] = useState<Date | null>(null);
  const [isLogInitLoaded, setIsLogInitLoaded] = useState(false);
  const [isRepayInitLoaded, setIsRepayInitLoaded] = useState(false);

  useFocusEffect(
    useCallback(() => {
      fetchInitialData();
    }, [fetchInitialData])
  );

  useEffect(() => {
    if (activeTab === "LOG" && !isLogInitLoaded) {
      fetchLogs();
      setIsLogInitLoaded(true);
    }
  }, [activeTab, fetchLogs, isLogInitLoaded]);

  useEffect(() => {
    if (
      activeTab === "REPAY" &&
      !isRepayInitLoaded &&
      agreement?.itemType === "MONEY"
    ) {
      fetchRepayments();
      setIsRepayInitLoaded(true);
    }
  }, [activeTab, isRepayInitLoaded, agreement?.itemType, fetchRepayments]);

  const isMoney = agreement?.itemType === "MONEY";
  const hideStatuses = ["PENDING", "REJECTED", "CANCELED"] as const;
  const canShowRepayTab =
    !!agreement && isMoney && !hideStatuses.includes(agreement.status as any);

  useEffect(() => {
    if (!canShowRepayTab && activeTab === "REPAY") {
      setActiveTab("INFO");
    }
  }, [canShowRepayTab, activeTab]);

  if (isLoading) {
    return (
      <View style={styles.centeredView}>
        <ActivityIndicator animating size="large" />
      </View>
    );
  }

  if (!agreement) {
    return (
      <View style={styles.centeredView}>
        <Appbar.Header style={{ height: 44 }}>
          <Appbar.BackAction onPress={() => navigation.goBack()} />
          <Appbar.Content title="" />
        </Appbar.Header>
      </View>
    );
  }

  const parseLocalDate = (s: string) => {
    const [y, m, d] = s.split("-").map(Number);
    return new Date(y, (m ?? 1) - 1, d ?? 1);
  };
  const addDays = (d: Date, n: number) => {
    const nd = new Date(d);
    nd.setDate(nd.getDate() + n);
    return nd;
  };
  const prevDue = agreement.dueDate
    ? parseLocalDate(agreement.dueDate)
    : new Date();
  const minSelectable = addDays(prevDue, 1);
  const startAt =
    agreement.debtorConfirmAt ||
    agreement.creditorConfirmAt ||
    agreement.createdAt ||
    null;
  const partnerName =
    agreement.myRole === "CREDITOR"
      ? agreement.debtorName
      : agreement.creditorName;
  const partnerPhone =
    agreement.myRole === "CREDITOR"
      ? agreement.debtorPhone
      : agreement.creditorPhone;
  const isItemLoan = agreement.itemType === "OBJECT";
  const hasSettlement =
    typeof agreement.amount === "number" ||
    typeof agreement.remainingAmount === "number";

  return (
    <>
      <Appbar.Header style={{ height: 44 }}>
        <Appbar.BackAction onPress={() => navigation.goBack()} />
        <Appbar.Content title="" />
      </Appbar.Header>

      <View style={{ backgroundColor: "#fff" }}>
        <DetailFilterPills
          value={activeTab}
          showRepay={canShowRepayTab}
          onChange={setActiveTab}
        />
      </View>

      {activeTab === "INFO" && (
        <ScrollView style={styles.container}>
          <AgreementInfoTab
            agreement={agreement}
            partnerName={partnerName}
            partnerPhone={partnerPhone}
            startAt={startAt}
            isItemLoan={isItemLoan}
            isMoney={isMoney}
            hasSettlement={hasSettlement}
          />
          <View style={{ height: 24 }} />
          <ActionButtons
            myRole={agreement.myRole}
            status={agreement.status}
            isMoney={isMoney}
            onAccept={async () => {
              try {
                await agreementAccept(agreementId);
                await fetchInitialData();
              } catch (e) {
                console.error("승인 중 에러", e);
              }
            }}
            onReject={async () => {
              try {
                await agreementReject(agreementId);
                await fetchInitialData();
              } catch (e) {
                console.error("거절 중 에러", e);
              }
            }}
            onCancel={async () => {
              try {
                await agreementCancel(agreementId);
                await fetchInitialData();
              } catch (e) {
                console.error("취소 중 에러", e);
              }
            }}
            onComplete={async () => {
              try {
                await agreementComplete(agreementId);
                await fetchInitialData();
              } catch (e) {
                console.error("완료 중 에러", e);
              }
            }}
            onExtendPress={() => setExtendVisible(true)}
            onRepayPress={() => setRepayVisible(true)}
          />
        </ScrollView>
      )}

      {activeTab === "LOG" && (
        <View style={styles.container}>
          <ActivityLogTab
            items={logItems}
            isLoading={isLogLoading}
            hasNext={logHasNext}
            onEndReached={fetchLogs}
          />
        </View>
      )}

      {activeTab === "REPAY" && canShowRepayTab && (
        <View style={styles.container}>
          <RepaymentTab
            remainingAmount={agreement.remainingAmount}
            items={repayItems}
            isLoading={isRepayLoading}
            hasNext={repayHasNext}
            onEndReached={fetchRepayments}
            canAct={agreement.myRole === "CREDITOR"}
            actingIds={repayActingIds}
            onConfirm={handleConfirmRepay}
            onReject={handleRejectRepay}
          />
        </View>
      )}

      <Portal>
        <Dialog
          visible={repayVisible}
          onDismiss={() => {
            setRepayVisible(false);
            setRepayAmount("");
          }}
        >
          <Dialog.Title>상환 확인 요청</Dialog.Title>
          <Dialog.Content>
            <TextInput
              label="상환 금액"
              keyboardType="numeric"
              value={repayAmount}
              onChangeText={setRepayAmount}
            />
          </Dialog.Content>
          <Dialog.Actions>
            <Button onPress={() => setRepayVisible(false)}>취소</Button>
            <Button
              onPress={async () => {
                try {
                  const res = await agreementTransaction(agreementId, {
                    amount: repayAmount,
                  });
                  setRepayVisible(false);
                  setRepayAmount("");
                  await fetchInitialData();
                  if (activeTab === "REPAY") fetchRepayments();
                } catch (error: any) {
                  console.error("상환 요청 실패:", error);
                  const msg =
                    error.response?.data?.message ||
                    error.message ||
                    "상환 요청에 실패했습니다.";

                  Alert.alert("오류", msg);
                }
              }}
            >
              확인
            </Button>
          </Dialog.Actions>
        </Dialog>
      </Portal>

      <DatePickerModal
        locale="ko"
        mode="single"
        visible={extendVisible}
        date={extendDate ?? minSelectable}
        validRange={{ startDate: minSelectable }}
        onDismiss={() => setExtendVisible(false)}
        onConfirm={async ({ date }) => {
          try {
            const adjusted = new Date(date!);
            adjusted.setDate(adjusted.getDate() + 1);
            await agreementExtend(agreementId, {
              dueAt: adjusted.toISOString(),
            });
            setExtendVisible(false);
            await fetchInitialData();
          } catch (error) {
            console.error("연장 요청 실패");
          }
        }}
      />
    </>
  );
};

function DetailFilterPills({
  value,
  showRepay,
  onChange,
}: {
  value: Tab;
  showRepay: boolean;
  onChange: (v: Tab) => void;
}) {
  return (
    <View style={styles.pillsRow}>
      <Pill
        active={value === "INFO"}
        bg="#F3F4F6"
        border="#E5E7EB"
        icon="information-outline"
        label="대여정보"
        textColor="#374151"
        onPress={() => onChange("INFO")}
      />
      <Pill
        active={value === "LOG"}
        bg="#EEF2FF"
        border="#E0E7FF"
        icon="history"
        label="활동로그"
        textColor="#3730A3"
        onPress={() => onChange("LOG")}
      />
      {showRepay && (
        <Pill
          active={value === "REPAY"}
          bg="#F5F3FF"
          border="#EDE9FE"
          icon="cash-multiple"
          label="상환기록"
          textColor="#7C3AED"
          onPress={() => onChange("REPAY")}
        />
      )}
    </View>
  );
}

function Pill({
  active,
  bg,
  border,
  icon,
  label,
  textColor,
  onPress,
}: {
  active: boolean;
  bg: string;
  border: string;
  icon: any;
  label: string;
  textColor: string;
  onPress: () => void;
}) {
  return (
    <TouchableOpacity
      activeOpacity={0.9}
      onPress={onPress}
      style={[
        styles.pill,
        { backgroundColor: bg, borderColor: border },
        active && styles.pillActive,
      ]}
    >
      <MaterialCommunityIcons name={icon} size={16} color={textColor} />
      <Text style={[styles.pillText, { color: textColor }]}>{label}</Text>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#fff",
  },
  centeredView: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
  },

  pillsRow: {
    flexDirection: "row",
    alignItems: "stretch",
    marginHorizontal: 14,
    marginVertical: 8,
  },
  pill: {
    flex: 1,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    gap: 6,
    borderWidth: 1,
    borderRadius: 999,
    paddingHorizontal: 12,
    paddingVertical: 8,
    minHeight: 36,
    marginRight: 8,
  },
  pillText: {
    fontSize: 13,
    fontWeight: "700",
    includeFontPadding: false,
    textAlignVertical: "center",
  },
  pillActive: {
    borderColor: "#111827",
    shadowColor: "#000",
    shadowOpacity: 0.06,
    shadowRadius: 6,
    shadowOffset: { width: 0, height: 3 },
    elevation: 2,
  },
});

export default MyAgreementDetailScreen;
