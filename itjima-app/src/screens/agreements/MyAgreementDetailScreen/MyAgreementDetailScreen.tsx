import React, { useCallback, useEffect, useState } from "react";
import { ScrollView, StyleSheet, View } from "react-native";
import {
  ActivityIndicator,
  Appbar,
  Button,
  Dialog,
  Portal,
  SegmentedButtons,
  TextInput,
} from "react-native-paper";
import { DatePickerModal } from "react-native-paper-dates";
import { useFocusEffect } from "@react-navigation/native";
import {
  agreementAccept,
  agreementCancel,
  agreementComplete,
  agreementExtend,
  agreementReject,
  agreementTransaction,
} from "../../../api/agreements";

import useAgreementDetail, {
  toLocalDateString,
} from "./hooks/useAgreementDetail";
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
  const isMoney = agreement.itemType === "MONEY";
  const isItemLoan = agreement.itemType === "OBJECT";
  const hasSettlement =
    typeof agreement.amount === "number" ||
    typeof agreement.remainingAmount === "number";

  const segmentedButtons = [
    {
      value: "INFO",
      label: "대여정보",
      style: styles.segmentedButton,
      labelStyle: styles.segmentedLabel,
    },
    {
      value: "LOG",
      label: "활동로그",
      style: styles.segmentedButton,
      labelStyle: styles.segmentedLabel,
    },
    ...(isMoney
      ? [
          {
            value: "REPAY",
            label: "상환기록",
            style: styles.segmentedButton,
            labelStyle: styles.segmentedLabel,
          } as const,
        ]
      : []),
  ] as const;

  return (
    <>
      <Appbar.Header style={{ height: 44 }}>
        <Appbar.BackAction onPress={() => navigation.goBack()} />
        <Appbar.Content title="" />
      </Appbar.Header>

      <View style={{ backgroundColor: "#fff" }}>
        <SegmentedButtons
          value={activeTab}
          onValueChange={(v) => setActiveTab(v as Tab)}
          style={styles.segmentedContainer}
          buttons={segmentedButtons as any}
          theme={{
            colors: {
              primary: "#fff",
              onPrimary: "#000",
              surface: "#f0f0f3",
              outline: "transparent",
            },
          }}
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

      {activeTab === "REPAY" && agreement.itemType === "MONEY" && (
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
        <Dialog visible={repayVisible} onDismiss={() => setRepayVisible(false)}>
          <Dialog.Title>상환 요청</Dialog.Title>
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
                  await agreementTransaction(agreementId, {
                    amount: repayAmount,
                  });
                  setRepayVisible(false);
                  await fetchInitialData();
                  if (activeTab === "REPAY") fetchRepayments();
                } catch (error) {
                  console.error("상환 요청에 실패했습니다.");
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
  segmentedContainer: {
    marginHorizontal: 14,
    marginVertical: 8,
    borderRadius: 6,
    height: 40,
    backgroundColor: "#f0f0f3",
  },
  segmentedButton: {
    flex: 1,
    borderRadius: 6,
  },
  segmentedLabel: {
    fontSize: 13,
    fontWeight: "600",
  },
});

export default MyAgreementDetailScreen;
