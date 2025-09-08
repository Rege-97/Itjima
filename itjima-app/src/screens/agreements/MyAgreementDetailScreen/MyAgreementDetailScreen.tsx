import React, { useCallback, useState } from "react";
import {
  Alert,
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
  getAgreementDetailApi,
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

/** 활동 로그 한 줄 */
const ActivityLogItem = ({
  type,
  message,
  createdAt,
}: {
  type?: string;
  message?: string;
  createdAt?: string;
}) => (
  <View style={styles.logItem}>
    <MaterialCommunityIcons
      name="history"
      size={18}
      color="#666"
      style={{ marginRight: 8 }}
    />
    <View style={{ flex: 1 }}>
      <Text style={styles.logMessage}>
        {message || type || "로그"}
      </Text>
      <Text style={styles.logDate}>{formatDate(createdAt || null)}</Text>
    </View>
  </View>
);

/** 상환 기록 한 줄 */
const RepaymentItem = ({
  amount,
  createdAt,
  memo,
}: {
  amount?: number | string;
  createdAt?: string;
  memo?: string;
}) => (
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
      <Text style={styles.logDate}>{formatDate(createdAt || null)}</Text>
    </View>
  </View>
);

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

  const fetchInitialData = useCallback(async () => {
    if (agreementId === undefined || agreementId === null) {
      Alert.alert("오류", "유효하지 않은 대여 정보입니다.");
      navigation.goBack();
      return;
    }
    setIsLoading(true);
    try {
      const { data } = await getAgreementDetailApi(agreementId);
      setAgreement(data.data);
    } catch (error) {
      Alert.alert("오류", "대여 정보를 불러올 수 없습니다.");
      navigation.goBack();
    } finally {
      setIsLoading(false);
    }
  }, [agreementId, navigation]);

  useFocusEffect(
    useCallback(() => {
      fetchInitialData();
    }, [fetchInitialData])
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

  // 탭 버튼 구성 (상환기록은 MONEY일 때만)
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

  /** INFO 탭 렌더 */
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
        {/* 내가 DEBTOR이고 PENDING 상태 → 승낙/거절 버튼 */}
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

        {/* 내가 DEBTOR이고 ACCEPTED 상태 && MONEY → 상환요청 */}
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

        {/* 내가 CREDITOR이고 PENDING → 취소 */}
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

        {/* 내가 CREDITOR이고 ACCEPTED/OVERDUE → 대여 완료 */}
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

        {/* 내가 CREDITOR이고 완료가 아니면 → 연장 */}
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

  /** 활동로그 탭 */
  const renderLog = () => {
    const logs: any[] =
      agreement.activityLogs ||
      agreement.logs ||
      []; // 서버 필드명 케이스 가드
    return (
      <>
        <Section title="활동 로그">
          {logs.length === 0 ? (
            <Text>활동 로그가 없습니다.</Text>
          ) : (
            <View>
              {logs.map((l) => (
                <ActivityLogItem
                  key={l.id ?? `${l.createdAt}-${Math.random()}`}
                  type={l.type}
                  message={l.message}
                  createdAt={l.createdAt}
                />
              ))}
            </View>
          )}
        </Section>
      </>
    );
  };

  /** 상환기록 탭 (MONEY 전용) */
  const renderRepay = () => {
    const txs: any[] =
      agreement.transactions ||
      agreement.repayments ||
      []; // 서버 필드명 케이스 가드
    return (
      <>
        <Section title="상환 기록">
          {typeof agreement.remainingAmount === "number" && (
            <InfoRow
              icon="cash-refund"
              label="남은 금액"
              value={`${agreement.remainingAmount.toLocaleString()} 원`}
            />
          )}
          <View style={{ height: 6 }} />
          {txs.length === 0 ? (
            <Text>상환 기록이 없습니다.</Text>
          ) : (
            <View>
              {txs.map((t) => (
                <RepaymentItem
                  key={t.id ?? `${t.createdAt}-${Math.random()}`}
                  amount={t.amount}
                  createdAt={t.createdAt || t.created_at}
                  memo={t.memo || t.note}
                />
              ))}
            </View>
          )}
        </Section>
      </>
    );
  };

  return (
    <>
      <Appbar.Header style={{ height: 44 }}>
        <Appbar.BackAction onPress={() => navigation.goBack()} />
        <Appbar.Content title="대여 상세" />
        <Appbar.Action icon="pencil" onPress={() => {}} />
      </Appbar.Header>

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

      <ScrollView style={styles.container}>
        {activeTab === "INFO" && renderInfo()}
        {activeTab === "LOG" && renderLog()}
        {activeTab === "REPAY" && isMoney && renderRepay()}
      </ScrollView>

      {/* 상환 요청 모달 */}
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
                  await fetchInitialData();
                } catch (e) {
                  Alert.alert("실패", "상환 요청에 실패했습니다.");
                }
              }}
            >
              확인
            </Button>
          </Dialog.Actions>
        </Dialog>
      </Portal>

      {/* 연장 모달 - DatePicker */}
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
  titleRow: { flexDirection: "row", justifyContent: "space-between", alignItems: "center" },
  partnerName: { fontSize: 22, fontWeight: "bold", color: "#222", flexShrink: 1 },

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
  sectionTitle: { fontWeight: "bold", marginBottom: 16, fontSize: 18, color: "#333" },
  sectionBody: { borderTopWidth: StyleSheet.hairlineWidth, borderTopColor: "transparent" },

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

  itemImage: { width: "100%", aspectRatio: 4 / 3, borderRadius: 10, backgroundColor: "#eee", marginBottom: 12 },
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

  // 로그/상환 공통 스타일
  logItem: {
    flexDirection: "row",
    alignItems: "flex-start",
    paddingVertical: 10,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: "#e6e6e6",
  },
  logMessage: { fontSize: 13, color: "#333" },
  logSub: { fontSize: 12, color: "#666", marginTop: 4 },
  logDate: { fontSize: 12, color: "#888", marginTop: 2 },
});

export default MyAgreementDetailScreen;
