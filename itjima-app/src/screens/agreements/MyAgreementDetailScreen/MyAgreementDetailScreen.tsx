import React, { useCallback, useEffect, useState } from "react";
import {
  Alert,
  FlatList,
  Image,
  Linking,
  ScrollView,
  StyleSheet,
  TouchableOpacity,
  View,
} from "react-native";
import {
  ActivityIndicator,
  Appbar,
  Button,
  Dialog,
  Divider,
  Portal,
  SegmentedButtons,
  Text,
  TextInput,
} from "react-native-paper";
import { useFocusEffect } from "@react-navigation/native";
import { MaterialCommunityIcons } from "@expo/vector-icons";
import {
  agreementAccept,
  agreementCancel,
  agreementComplete,
  agreementExtend,
  agreementReject,
  agreementTransaction,
  confirmTransactionApi,
  getAgreementDetailApi,
  getAgreementLogsApi,
  getAgreementTransactionsApi,
  rejectTransactionApi, // ← 활동로그 API (agreementId, lastId?)
} from "../../../api/agreements";
import { IMG_BASE_URL } from "@env";
import { DatePickerModal } from "react-native-paper-dates";

type Tab = "INFO" | "LOG" | "REPAY";

const formatDate = (dateString: string | null) => {
  if (!dateString) return "정보 없음";
  const d = new Date(dateString);
  return d.toLocaleString("ko-KR", {
    year: "numeric",
    month: "long",
    day: "numeric",
    hour12: false,
  });
};

const toLocalDateString = (d: Date) => {
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
};

const parseLocalDate = (s: string) => {
  const [y, m, d] = s.split("-").map(Number);
  return new Date(y, (m ?? 1) - 1, d ?? 1);
};

const addDays = (d: Date, n: number) => {
  const nd = new Date(d);
  nd.setDate(nd.getDate() + n);
  return nd;
};

const resolveImageUrl = (path?: string | null) => {
  if (!path) return "https://via.placeholder.com/800x600";
  return (IMG_BASE_URL || "") + path;
};

const AgreementStatusBadge = ({
  status,
  overdueReturn,
}: {
  status: string;
  overdueReturn: boolean;
}) => {
  let backgroundColor = "#ccc";
  let label = status;
  switch (status) {
    case "PENDING":
      backgroundColor = "#FFC107";
      label = "승인 대기 중";
      break;
    case "ACCEPTED":
      backgroundColor = "#2196F3";
      label = "대여중";
      break;
    case "REJECTED":
      backgroundColor = "#F44336";
      label = "거절됨";
      break;
    case "COMPLETED":
      if (overdueReturn) {
        backgroundColor = "#FF5722";
        label = "연체반납";
      } else {
        backgroundColor = "#4CAF50";
        label = "완료됨";
      }
      break;
    case "CANCELED":
      backgroundColor = "#9E9E9E";
      label = "취소됨";
      break;
    case "OVERDUE":
      backgroundColor = "#E91E63";
      label = "연체됨";
      break;
  }
  return (
    <View style={[styles.badge, { backgroundColor }]}>
      <Text style={styles.badgeText}>{label}</Text>
    </View>
  );
};

const RoleBadge = ({ myRole }: { myRole: "DEBTOR" | "CREDITOR" }) => {
  const isDebtor = myRole === "DEBTOR";
  const backgroundColor = isDebtor ? "#4CAF50" : "#5c36f4";
  const text = isDebtor ? "빌림" : "빌려줌";
  return (
    <View style={[styles.badge, { backgroundColor, marginRight: 8 }]}>
      <Text style={styles.badgeText}>{text}</Text>
    </View>
  );
};

const InfoRow = ({
  icon,
  label,
  value,
  onPress,
}: {
  icon: any;
  label: string;
  value: string;
  onPress?: () => void;
}) => (
  <TouchableOpacity disabled={!onPress} onPress={onPress} activeOpacity={0.6}>
    <View style={styles.infoRow}>
      <MaterialCommunityIcons
        name={icon}
        size={20}
        color="#666"
        style={styles.infoIcon}
      />
      <Text style={styles.infoLabel}>{label}</Text>
      <Text style={styles.infoValue}>{value}</Text>
    </View>
  </TouchableOpacity>
);

const Section = ({
  title,
  children,
}: {
  title: string;
  children: React.ReactNode;
}) => (
  <View style={styles.section}>
    <Text style={styles.sectionTitle}>{title}</Text>
    <View style={styles.sectionBody}>{children}</View>
  </View>
);

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

const repaymentStatusMap: Record<string, string> = {
  PENDING: "대기중",
  CONFIRMED: "승인됨",
  REJECTED: "거절됨",
};

const ActivityLogItem = ({
  userName,
  action,
  createdAt,
}: {
  userName?: string;
  action?: string;
  detail?: string;
  createdAt?: string;
}) => {
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
        <Text style={styles.logDate}>{createdAt}</Text>
      </View>
    </View>
  );
};

const RepaymentItem = ({
  id,
  amount,
  createdAt,
  memo,
  status,
  showActions,
  onConfirm,
  onReject,
  acting,
}: {
  id?: number;
  amount?: number | string;
  createdAt?: string;
  memo?: string;
  status?: string;
  showActions?: boolean;
  onConfirm?: () => void;
  onReject?: () => void;
  acting?: boolean;
}) => {
  const statusLabel = status ? repaymentStatusMap[status] || status : "";
  return (
    <View style={styles.logItem}>
      <MaterialCommunityIcons
        name="cash-check"
        size={18}
        color="#666"
        style={{ marginRight: 8 }}
      />
      <View style={{ flex: 1 }}>
        <Text style={styles.logMessage}>
          {typeof amount === "number"
            ? `${amount.toLocaleString()} 원`
            : amount
            ? `${amount} 원`
            : "금액 미상"}
        </Text>
        {!!memo && <Text style={styles.logSub}>{memo}</Text>}

        {/* 날짜/상태(왼쪽)  +  액션 버튼(오른쪽) */}
        <View style={styles.repayBottomRow}>
          <Text style={styles.logDate}>
            {formatDate(createdAt || null)} {statusLabel && `(${statusLabel})`}
          </Text>

          {showActions && (
            <View style={styles.repayActionsRow}>
              <Button
                mode="contained"
                compact
                style={styles.repayActionBtn}
                disabled={acting}
                onPress={onConfirm}
              >
                승인
              </Button>
              <Button
                mode="outlined"
                compact
                style={styles.repayActionBtn}
                disabled={acting}
                onPress={onReject}
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

const MyAgreementDetailScreen = ({ route, navigation }: any) => {
  const { agreementId } = route.params;

  const [agreement, setAgreement] = useState<any>(null);
  const [isLoading, setIsLoading] = useState(true);

  // Segmented tab
  const [activeTab, setActiveTab] = useState<Tab>("INFO");

  // 상환 요청
  const [repayVisible, setRepayVisible] = useState(false);
  const [repayAmount, setRepayAmount] = useState("");

  // 연장
  const [extendVisible, setExtendVisible] = useState(false);
  const [extendDate, setExtendDate] = useState<Date | null>(null);

  // 활동로그 상태
  const [logItems, setLogItems] = useState<any[]>([]);
  const [logHasNext, setLogHasNext] = useState(true);
  const [logLastId, setLogLastId] = useState<number | null>(null);
  const [isLogLoading, setIsLogLoading] = useState(false);
  const [isLogInitLoaded, setIsLogInitLoaded] = useState(false);

  const [repayItems, setRepayItems] = useState<any[]>([]);
  const [repayHasNext, setRepayHasNext] = useState(true);
  const [repayLastId, setRepayLastId] = useState<number | null>(null);
  const [isRepayLoading, setIsRepayLoading] = useState(false);
  const [isRepayInitLoaded, setIsRepayInitLoaded] = useState(false);

  const [repayActingIds, setRepayActingIds] = useState<Set<number>>(new Set());

  const dedupeById = (list: any[]) => [
    ...new Map(list.map((i) => [i.id, i])).values(),
  ];

  const fetchInitialData = useCallback(async () => {
    if (agreementId === undefined || agreementId === null) {
      Alert.alert("오류", "유효하지 않은 대여 정보입니다.");
      navigation.goBack();
      return;
    }
    setIsLoading(true);
    try {
      const { data } = await getAgreementDetailApi(agreementId);
      setAgreement(data?.data ?? data);
    } catch (error) {
      Alert.alert("오류", "대여 정보를 불러올 수 없습니다.");
      navigation.goBack();
    } finally {
      setIsLoading(false);
    }
  }, [agreementId, navigation]);

  const fetchLogs = useCallback(async () => {
    setIsLogLoading(true);
    try {
      const response = await getAgreementLogsApi(agreementId, logLastId!);

      const fetchedData = response.data.data;

      setLogItems(fetchedData.items || []);
      setLogLastId(fetchedData.lastId);
      setLogHasNext(fetchedData.hasNext);
    } catch (error) {
      console.error("활동로그 목록을 불러오는데 실패했습니다:", error);
    } finally {
      setIsLogLoading(false);
    }
  }, [agreementId, isLogLoading, logHasNext, logLastId]);

  const fetchRepayments = useCallback(async () => {
    if (isRepayLoading || !repayHasNext) return;
    setIsRepayLoading(true);
    try {
      const { data } = await getAgreementTransactionsApi(
        agreementId,
        repayLastId!
      );
      const payload = data?.data ?? data;

      const nextItems = payload.items || [];
      const merged = dedupeById([...repayItems, ...nextItems]);

      setRepayItems(merged);
      setRepayLastId(payload.lastId ?? null);
      setRepayHasNext(!!payload.hasNext);
    } catch (e) {
      console.error("상환 기록을 불러오는데 실패했습니다:", e);
    } finally {
      setIsRepayLoading(false);
    }
  }, [agreementId, isRepayLoading, repayHasNext, repayLastId, repayItems]);

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
      agreement.itemType === "MONEY"
    ) {
      fetchRepayments();
      setIsRepayInitLoaded(true);
    }
  }, [activeTab, isRepayInitLoaded, agreement?.itemType, fetchRepayments]);

  const handleConfirmRepay = useCallback(
    async (txId: number) => {
      if (repayActingIds.has(txId)) return;
      setRepayActingIds((prev) => new Set(prev).add(txId));
      try {
        await confirmTransactionApi(txId);
        Alert.alert("승인됨", "상환이 승인되었습니다.");

        // 리스트/합계 갱신
        setRepayItems([]);
        setRepayLastId(null);
        setRepayHasNext(true);
        setIsRepayInitLoaded(false);
        await fetchInitialData();
        if (activeTab === "REPAY") {
          await fetchRepayments();
        }
      } catch (e) {
        console.error(e);
        Alert.alert("실패", "상환 승인에 실패했습니다.");
      } finally {
        setRepayActingIds((prev) => {
          const next = new Set(prev);
          next.delete(txId);
          return next;
        });
      }
    },
    [activeTab, fetchInitialData, fetchRepayments, repayActingIds]
  );

  const handleRejectRepay = useCallback(
    async (txId: number) => {
      if (repayActingIds.has(txId)) return;
      setRepayActingIds((prev) => new Set(prev).add(txId));
      try {
        await rejectTransactionApi(txId);
        Alert.alert("거절됨", "상환이 거절되었습니다.");

        setRepayItems([]);
        setRepayLastId(null);
        setRepayHasNext(true);
        setIsRepayInitLoaded(false);
        await fetchInitialData();
        if (activeTab === "REPAY") {
          await fetchRepayments();
        }
      } catch (e) {
        console.error(e);
        Alert.alert("실패", "상환 거절에 실패했습니다.");
      } finally {
        setRepayActingIds((prev) => {
          const next = new Set(prev);
          next.delete(txId);
          return next;
        });
      }
    },
    [activeTab, fetchInitialData, fetchRepayments, repayActingIds]
  );

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
        <Text>대여 정보를 찾을 수 없습니다.</Text>
      </View>
    );
  }

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

  const renderInfo = () => (
    <>
      <View style={styles.topBlock}>
        <View style={styles.titleRow}>
          <Text style={styles.partnerName}>{partnerName || "상대방"}</Text>
          <View style={styles.badgesRow}>
            <RoleBadge myRole={agreement.myRole} />
            <AgreementStatusBadge
              status={agreement.status}
              overdueReturn={!!agreement.isOverdueReturn}
            />
          </View>
        </View>

        {partnerPhone ? (
          <View style={{ marginTop: 10 }}>
            <InfoRow
              icon="phone"
              label="연락처"
              value={partnerPhone}
              onPress={() => Linking.openURL(`tel:${partnerPhone}`)}
            />
          </View>
        ) : null}

        {agreement.terms ? (
          <View style={{ marginTop: 6 }}>
            <InfoRow
              icon="file-document-outline"
              label="대여 상세"
              value={agreement.terms}
            />
          </View>
        ) : null}
      </View>

      <Divider style={styles.divider} />

      <Section title="대여 기간">
        <InfoRow
          icon="calendar-arrow-right"
          label="시작일"
          value={formatDate(startAt)}
        />
        <InfoRow
          icon="calendar-arrow-left"
          label="반납 예정일"
          value={formatDate(agreement.dueDate)}
        />
        {agreement.returnDate ? (
          <InfoRow
            icon="calendar-check"
            label="반납일"
            value={formatDate(agreement.returnDate)}
          />
        ) : null}
        {typeof agreement.rentalDays === "number" && (
          <InfoRow
            icon="timelapse"
            label="대여 기간"
            value={`${agreement.rentalDays}일`}
          />
        )}
      </Section>

      <Divider style={styles.divider} />

      {isItemLoan ? (
        <>
          <Section title="물품 정보">
            <Image
              source={{ uri: resolveImageUrl(agreement.itemFileUrl) }}
              style={styles.itemImage}
            />
            <InfoRow
              icon="cube-outline"
              label="물품명"
              value={agreement.itemTitle || "제목 없음"}
            />
            <InfoRow
              icon="text"
              label="물품 상세"
              value={agreement.itemDescription || "설명 없음"}
            />
          </Section>
          <Divider style={styles.divider} />
        </>
      ) : (
        <>
          <Section title="대출 정보">
            <InfoRow
              icon="cash-multiple"
              label="빌린 금액"
              value={
                typeof agreement.amount === "number"
                  ? `${agreement.amount.toLocaleString()} 원`
                  : "정보 없음"
              }
            />
            <InfoRow
              icon="cash-refund"
              label="남은 금액"
              value={
                typeof agreement.remainingAmount === "number"
                  ? `${agreement.remainingAmount.toLocaleString()} 원`
                  : "정보 없음"
              }
            />
          </Section>
          <Divider style={styles.divider} />
        </>
      )}

      {hasSettlement && isItemLoan && (
        <>
          <Section title="정산 정보">
            {typeof agreement.amount === "number" && (
              <InfoRow
                icon="cash-multiple"
                label="거래 금액"
                value={`${agreement.amount.toLocaleString()} 원`}
              />
            )}
            {typeof agreement.remainingAmount === "number" && (
              <InfoRow
                icon="cash-refund"
                label="남은 금액"
                value={`${agreement.remainingAmount.toLocaleString()} 원`}
              />
            )}
          </Section>
          <Divider style={styles.divider} />
        </>
      )}

      <View style={{ height: 24 }} />
      <View style={styles.actions}>
        {agreement.myRole === "DEBTOR" && agreement.status === "PENDING" && (
          <View style={styles.row}>
            <Button
              mode="contained"
              style={styles.button}
              onPress={async () => {
                try {
                  await agreementAccept(agreementId);
                  Alert.alert("승인 성공", "대여가 승인되었습니다.");
                  await fetchInitialData();
                } catch (error) {
                  console.error("승인 중 에러가 발생했습니다.", error);
                }
              }}
            >
              대여 승낙
            </Button>
            <Button
              mode="outlined"
              style={styles.button}
              onPress={async () => {
                try {
                  await agreementReject(agreementId);
                  Alert.alert("거절 성공", "대여가 거절되었습니다.");
                  await fetchInitialData();
                } catch (error) {
                  console.error("거절 중 에러가 발생했습니다.", error);
                }
              }}
            >
              대여 거절
            </Button>
          </View>
        )}

        {agreement.myRole === "DEBTOR" &&
          agreement.status === "ACCEPTED" &&
          isMoney && (
            <Button
              mode="contained"
              style={styles.button}
              onPress={() => setRepayVisible(true)}
            >
              상환 요청
            </Button>
          )}

        {agreement.myRole === "CREDITOR" && agreement.status === "PENDING" && (
          <Button
            mode="outlined"
            style={styles.button}
            onPress={async () => {
              try {
                await agreementCancel(agreementId);
                Alert.alert("취소 성공", "대여가 취소되었습니다.");
                await fetchInitialData();
              } catch (error) {
                console.error("취소 중 에러가 발생했습니다.", error);
              }
            }}
          >
            대여 취소
          </Button>
        )}

        {agreement.myRole === "CREDITOR" &&
          (agreement.status === "ACCEPTED" ||
            agreement.status === "OVERDUE") && (
            <Button
              mode="contained"
              style={styles.button}
              onPress={async () => {
                try {
                  await agreementComplete(agreementId);
                  Alert.alert("완료 성공", "대여가 완료되었습니다.");
                  await fetchInitialData();
                } catch (error) {
                  console.error("완료 중 에러가 발생했습니다.", error);
                }
              }}
            >
              대여 완료
            </Button>
          )}

        {agreement.myRole === "CREDITOR" &&
          agreement.status !== "COMPLETED" && (
            <Button
              mode="outlined"
              style={styles.button}
              onPress={() => setExtendVisible(true)}
            >
              대여기간 연장
            </Button>
          )}
      </View>
    </>
  );

  const renderLog = () => (
    <Section title="활동 로그">
      <FlatList
        data={logItems}
        keyExtractor={(item) => String(item.id ?? item.createdAt)}
        renderItem={({ item }) => (
          <ActivityLogItem
            userName={item.userName}
            action={item.action}
            detail={item.detail}
            createdAt={item.createdAt}
          />
        )}
        ListFooterComponent={
          isLogLoading ? (
            <View style={{ paddingVertical: 12 }}>
              <ActivityIndicator animating />
            </View>
          ) : !logHasNext && logItems.length > 0 ? (
            <Text style={styles.endText}>마지막 로그입니다.</Text>
          ) : null
        }
        onEndReachedThreshold={0.4}
        onEndReached={() => {
          if (logHasNext && !isLogLoading) {
            fetchLogs();
          }
        }}
      />
    </Section>
  );

  const renderRepay = () => (
    <Section title="상환 기록">
      {typeof agreement.remainingAmount === "number" && (
        <InfoRow
          icon="cash-refund"
          label="남은 금액"
          value={`${agreement.remainingAmount.toLocaleString()} 원`}
        />
      )}
      <View style={{ height: 6 }} />

      <FlatList
        data={repayItems}
        keyExtractor={(item) => String(item.id ?? item.createdAt)}
        renderItem={({ item }) => (
          <RepaymentItem
            id={item.id}
            amount={item.amount}
            createdAt={item.createdAt || item.created_at}
            memo={item.memo || item.note}
            status={item.status}
            showActions={
              agreement.myRole === "CREDITOR" && item.status === "PENDING"
            }
            acting={repayActingIds.has(item.id)}
            onConfirm={() => handleConfirmRepay(item.id)}
            onReject={() => handleRejectRepay(item.id)}
          />
        )}
        ListEmptyComponent={
          !isRepayLoading ? (
            <Text style={styles.emptyText}>상환 기록이 없습니다.</Text>
          ) : null
        }
        ListFooterComponent={
          isRepayLoading ? (
            <View style={{ paddingVertical: 12 }}>
              <ActivityIndicator animating />
            </View>
          ) : !repayHasNext && repayItems.length > 0 ? (
            <Text style={styles.endText}>마지막 상환 기록입니다.</Text>
          ) : null
        }
        onEndReachedThreshold={0.4}
        onEndReached={() => {
          if (repayHasNext && !isRepayLoading) {
            fetchRepayments();
          }
        }}
      />
    </Section>
  );

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
        <ScrollView style={styles.container}>{renderInfo()}</ScrollView>
      )}
      {activeTab === "LOG" && (
        <View style={styles.container}>{renderLog()}</View>
      )}
      {activeTab === "REPAY" && agreement.itemType === "MONEY" && (
        <View style={styles.container}>{renderRepay()}</View>
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
                  Alert.alert("요청 성공", `${repayAmount}원 상환 요청`);
                  setRepayVisible(false);
                  setRepayItems([]);
                  setRepayLastId(null);
                  setRepayHasNext(true);
                  setIsRepayInitLoaded(false);

                  await fetchInitialData();

                  if (activeTab === "REPAY") {
                    fetchRepayments();
                  }
                } catch (error) {
                  console.error("상환 요청에 실패했습니다.");
                  Alert.alert("실패", "상환 요청에 실패했습니다.");
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
            await agreementExtend(agreementId, {
              dueAt: toLocalDateString(date!),
            });
            Alert.alert(
              "성공",
              `반납 예정일을 ${toLocalDateString(date!)}로 연장했습니다.`
            );
            setExtendVisible(false);
            await fetchInitialData();
          } catch (error) {
            console.error("연장 요청에 실패했습니다.");
            Alert.alert("실패", "연장 요청에 실패했습니다.");
          }
        }}
      />
    </>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: "#fff" },
  centeredView: { flex: 1, justifyContent: "center", alignItems: "center" },

  topBlock: { paddingHorizontal: 16, paddingTop: 20, paddingBottom: 14 },
  titleRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  partnerName: {
    fontSize: 22,
    fontWeight: "bold",
    color: "#222",
    flexShrink: 1,
  },

  badgesRow: { flexDirection: "row", alignItems: "center" },
  badge: {
    borderRadius: 6,
    paddingHorizontal: 10,
    paddingVertical: 4,
    minWidth: 70,
    height: 30,
    justifyContent: "center",
    alignItems: "center",
  },
  badgeText: { fontSize: 12, fontWeight: "bold", color: "#fff" },

  section: { paddingHorizontal: 16, paddingVertical: 12 },
  sectionTitle: {
    fontWeight: "bold",
    marginBottom: 16,
    fontSize: 18,
    color: "#333",
  },
  sectionBody: {
    borderTopWidth: StyleSheet.hairlineWidth,
    borderTopColor: "transparent",
  },

  divider: { height: 10, backgroundColor: "#f4f5f7" },

  infoRow: {
    flexDirection: "row",
    alignItems: "center",
    paddingVertical: 10,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: "#e6e6e6",
  },
  infoIcon: { marginRight: 10 },
  infoLabel: { fontSize: 13, color: "#666", minWidth: 90 },
  infoValue: { fontSize: 13, color: "#333", flex: 1 },

  itemImage: {
    width: "100%",
    aspectRatio: 4 / 3,
    borderRadius: 10,
    backgroundColor: "#eee",
    marginBottom: 12,
  },
  actions: { paddingHorizontal: 16, paddingVertical: 12 },
  row: { flexDirection: "row", justifyContent: "space-between" },
  button: { flex: 1, marginHorizontal: 4, marginVertical: 6 },

  segmentedContainer: {
    marginHorizontal: 14,
    marginVertical: 8,
    borderRadius: 6,
    height: 40,
    backgroundColor: "#f0f0f3",
  },
  segmentedButton: { flex: 1, borderRadius: 6 },
  segmentedLabel: { fontSize: 13, fontWeight: "600" },

  logItem: {
    flexDirection: "row",
    alignItems: "flex-start",
    paddingVertical: 15,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: "#e6e6e6",
  },
  logMessage: { fontSize: 16, color: "#333" },
  logSub: { fontSize: 12, color: "#666", marginTop: 4 },
  logDate: { fontSize: 12, color: "#888", marginTop: 2 },

  emptyText: { fontSize: 13, color: "#777" },
  endText: { fontSize: 12, color: "#999", textAlign: "center", marginTop: 8 },
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
});

export default MyAgreementDetailScreen;
