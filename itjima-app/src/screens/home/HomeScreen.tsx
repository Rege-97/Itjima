import React, { useEffect, useState, useCallback } from "react";
import {
  SafeAreaView,
  ScrollView,
  View,
  StyleSheet,
  Image,
  TouchableOpacity,
  FlatList,
} from "react-native";
import { Text, ActivityIndicator, Portal, Dialog, Button } from "react-native-paper";
import { MaterialCommunityIcons } from "@expo/vector-icons";
import { useNavigation } from "@react-navigation/native";
import { getDashboardApi, getPendingAPi } from "../../api/dashboards";
import { IMG_BASE_URL } from "@env";

type ComingReturn = {
  id: number;
  amount?: number | null;
  dueAt: string;
  role: "CREDITOR" | "DEBTOR";
  itemTitle: string;
  itemDescription?: string;
  itemFileUrl?: string;
  daysLeft: number;
};

type OverDue = {
  id: number;
  amount?: number | null;
  dueAt: string;
  role: "CREDITOR" | "DEBTOR";
  itemTitle: string;
  itemDescription?: string;
  itemFileUrl?: string;
  overDays: number;
};

type PendingItem = {
  source: "AGREEMENT" | string;
  id: number;
  status: string;
  pendingUser: string;
  description: string;
  cursorKey: number;
  createdAt?: string;
};

const normalize = (v: any) => (Array.isArray(v) ? v : v ? [v] : []);

// 간단 상대시간
const timeAgo = (iso?: string) => {
  if (!iso) return "";
  const diff = Date.now() - new Date(iso).getTime();
  const m = Math.floor(diff / 60000);
  if (m < 1) return "방금 전";
  if (m < 60) return `${m}분 전`;
  const h = Math.floor(m / 60);
  if (h < 24) return `${h}시간 전`;
  const d = Math.floor(h / 24);
  return `${d}일 전`;
};

export default function HomeScreen() {
  const navigation = useNavigation<any>();

  // 상세 이동
  const goToAgreement = useCallback(
    (agreementId: number) => {
      navigation.navigate("AgreementList", {
        screen: "MyAgreementDetail",
        params: { agreementId },
      });
    },
    [navigation]
  );

  const [name, setName] = useState<string>("");
  const [borrowedCount, setBorrowedCount] = useState<number>(0);
  const [lentCount, setLentCount] = useState<number>(0);
  const [comingReturns, setComingReturns] = useState<ComingReturn[]>([]);
  const [overDues, setOverDues] = useState<OverDue[]>([]);
  const [showAllComing, setShowAllComing] = useState(false);
  const [showAllOverdue, setShowAllOverdue] = useState(false);
  const [pendingCount, setPendingCount] = useState<number>(0); // 🔔 배지용 카운트

  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // 🔔 승인 필요 모달 상태
  const [pendingOpen, setPendingOpen] = useState(false);
  const [pendings, setPendings] = useState<PendingItem[]>([]);
  const [pendingHasNext, setPendingHasNext] = useState<boolean>(true);
  const [pendingLastId, setPendingLastId] = useState<number | null>(null);
  const [pendingLoading, setPendingLoading] = useState<boolean>(false);

  // 커링된 API
  const pendingFetcher = getPendingAPi();

  useEffect(() => {
    const fetchDashboard = async () => {
      try {
        const res = await getDashboardApi();
        const fetchedData = res.data.data;

        // 이름
        setName(fetchedData?.name || fetchedData?.userName || fetchedData?.username || "");

        // 카운트(배열/단일 객체 둘 다 대응)
        const countsData = fetchedData?.counts;
        if (Array.isArray(countsData)) {
          for (let c of countsData) {
            if (c.role === "CREDITOR") setLentCount(c.count);
            else setBorrowedCount(c.count);
          }
        } else if (countsData && typeof countsData === "object") {
          if (countsData.role === "CREDITOR") setLentCount(countsData.count || 0);
          // 필요하면 DEBTOR 카운트도 서버에서 보내주게 확장
        }

        // 서머리에서 pendingCount 받기
        setPendingCount(Number(fetchedData?.pendingCount) || 0);

        // 리스트
        setComingReturns(normalize(fetchedData?.comingReturns).slice(0, 5));
        setOverDues(normalize(fetchedData?.overDues || fetchedData?.overdues).slice(0, 5));
      } catch {
        setError("대시보드 데이터를 불러오지 못했습니다.");
      } finally {
        setIsLoading(false);
      }
    };
    fetchDashboard();
  }, []);

  // 모달 열 때 최초 로드
  const openPending = useCallback(async () => {
    setPendingOpen(true);
    if (pendings.length === 0) {
      await loadPendings(true);
    }
  }, [pendings.length]);

  // 승인 필요 목록 로드(초기/추가)
  const loadPendings = useCallback(
    async (initial = false) => {
      if (pendingLoading) return;
      setPendingLoading(true);
      try {
        const res = await pendingFetcher(initial ? undefined : pendingLastId ?? undefined);
        const data = res.data?.data || res.data || {};
        const items: PendingItem[] = data.items || [];
        const nextHas = !!data.hasNext;

        const nextLast =
          typeof data.lastId === "number" && data.lastId > 0
            ? data.lastId
            : items.length > 0
            ? items[items.length - 1].cursorKey
            : pendingLastId;

        setPendings((prev) => (initial ? items : [...prev, ...items]));
        setPendingHasNext(nextHas);
        setPendingLastId(nextLast ?? null);
      } catch {
        // noop
      } finally {
        setPendingLoading(false);
      }
    },
    [pendingFetcher, pendingLastId, pendingLoading]
  );

  if (isLoading) {
    return (
      <SafeAreaView style={styles.center}>
        <ActivityIndicator size="large" />
        <Text>불러오는 중...</Text>
      </SafeAreaView>
    );
  }
  if (error) {
    return (
      <SafeAreaView style={styles.center}>
        <Text>{error}</Text>
      </SafeAreaView>
    );
  }

  const visibleComing = showAllComing ? comingReturns : comingReturns.slice(0, 2);
  const visibleOverdues = showAllOverdue ? overDues : overDues.slice(0, 2);

  return (
    <SafeAreaView style={{ flex: 1 }}>
      <ScrollView contentContainerStyle={styles.container}>
        {/* 헤더 */}
        <View style={styles.headerRow}>
          <Text variant="titleLarge" style={styles.greet}>
            안녕하세요{name ? `, ${name}` : ""}님!
          </Text>
          <TouchableOpacity onPress={openPending} hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}>
            <View style={{ position: "relative" }}>
              <MaterialCommunityIcons name="bell-outline" size={22} color="#6b7280" />
              {pendingCount > 0 && (
                <View style={styles.badge}>
                  <Text style={styles.badgeText}>
                    {pendingCount > 99 ? "99+" : pendingCount}
                  </Text>
                </View>
              )}
            </View>
          </TouchableOpacity>
        </View>

        {/* 통계 박스 */}
        <View style={styles.statsRow}>
          <StatBox title="빌려준 물건" count={lentCount} tone="orange" />
          <StatBox title="빌린 물건" count={borrowedCount} tone="blue" />
        </View>

        {/* 반납 임박 */}
        <View style={styles.section}>
          <View style={styles.sectionHeader}>
            <Text variant="titleMedium" style={{ fontWeight: "700" }}>
              반납일이 얼마 안 남았어요!
            </Text>
            <View style={{ flexDirection: "row", alignItems: "center", gap: 12 }}>
              {comingReturns.length > 2 && (
                <TouchableOpacity onPress={() => setShowAllComing((v) => !v)}>
                  <Text style={styles.moreBtn}>{showAllComing ? "간략히" : "더보기"}</Text>
                </TouchableOpacity>
              )}
            </View>
          </View>

          {comingReturns.length === 0 ? (
            <View style={styles.emptyBox}>
              <Text style={{ color: "#6b7280" }}>반납 예정 항목이 없습니다.</Text>
            </View>
          ) : (
            visibleComing.map((it) => (
              <TouchableOpacity key={it.id} activeOpacity={0.7} onPress={() => goToAgreement(it.id)}>
                <ComingReturnRow item={it} />
              </TouchableOpacity>
            ))
          )}
        </View>

        {/* 연체 중 */}
        <View style={styles.section}>
          <View style={styles.sectionHeader}>
            <Text variant="titleMedium" style={{ fontWeight: "700", marginTop: 10 }}>
              연체 중! 약속한 날짜가 지났어요
            </Text>
            <View style={{ flexDirection: "row", alignItems: "center", gap: 12 }}>
              {overDues.length > 2 && (
                <TouchableOpacity onPress={() => setShowAllOverdue((v) => !v)}>
                  <Text style={styles.moreBtn}>{showAllOverdue ? "간략히" : "더보기"}</Text>
                </TouchableOpacity>
              )}
            </View>
          </View>

          {overDues.length === 0 ? (
            <View style={styles.emptyBox}>
              <Text style={{ color: "#6b7280" }}>연체된 항목이 없습니다.</Text>
            </View>
          ) : (
            visibleOverdues.map((it) => (
              <TouchableOpacity key={it.id} activeOpacity={0.7} onPress={() => goToAgreement(it.id)}>
                <OverDueRow item={it} />
              </TouchableOpacity>
            ))
          )}
        </View>
      </ScrollView>

      {/* 🔔 승인 필요 모달 (화이트톤, 미니멀) */}
      <Portal>
        <Dialog
          visible={pendingOpen}
          onDismiss={() => setPendingOpen(false)}
          style={styles.dialog}
        >
          <Dialog.Title>
            <View style={styles.dialogTitleRow}>
              <MaterialCommunityIcons name="bell-outline" size={18} color="#111827" />
              <Text style={styles.dialogTitleText}>승인 필요</Text>
            </View>
          </Dialog.Title>

          <Dialog.Content style={styles.dialogContent}>
            {pendings.length === 0 && !pendingLoading ? (
              <View style={[styles.emptyBox, { marginTop: 8 }]}>
                <Text style={{ color: "#6b7280" }}>승인할 요청이 없습니다.</Text>
              </View>
            ) : (
              <FlatList
                data={pendings}
                keyExtractor={(it) => `${it.cursorKey || it.id}`}
                ItemSeparatorComponent={() => <View style={{ height: 8 }} />}
                onEndReachedThreshold={0.2}
                onEndReached={() => {
                  if (pendingHasNext && !pendingLoading) loadPendings(false);
                }}
                ListFooterComponent={
                  pendingLoading ? (
                    <View style={{ paddingVertical: 12, alignItems: "center" }}>
                      <ActivityIndicator />
                    </View>
                  ) : null
                }
                style={{ maxHeight: 420 }}
                renderItem={({ item }) => (
                  <TouchableOpacity
                    activeOpacity={0.7}
                    onPress={() => {
                      setPendingOpen(false);
                      goToAgreement(item.id);
                    }}
                  >
                    <PendingRow item={item} />
                  </TouchableOpacity>
                )}
              />
            )}
          </Dialog.Content>
          <Dialog.Actions style={styles.dialogActions}>
            <Button onPress={() => setPendingOpen(false)} textColor="#111827">닫기</Button>
          </Dialog.Actions>
        </Dialog>
      </Portal>
    </SafeAreaView>
  );
}

/** 카드 없이 만든 통계 박스 */
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
          <Text style={[styles.statTitle, { color: colors.text }]}>{title}</Text>
          <Text style={styles.statCount}>{count.toLocaleString()}개</Text>
        </View>
        <MaterialCommunityIcons name="cube-outline" size={28} color={colors.icon} />
      </View>
    </View>
  );
}

function ComingReturnRow({ item }: { item: ComingReturn }) {
  const isMoney = item.amount != null;
  const tagStyle = isMoney ? styles.moneyTag : styles.itemTag;
  const tagText = isMoney ? "금전대여" : "물품대여";
  const due = (item.dueAt || "").slice(0, 10);

  return (
    <View style={styles.rowBox}>
      {item.itemFileUrl ? (
        <Image source={{ uri: IMG_BASE_URL + item.itemFileUrl }} style={styles.thumb} />
      ) : (
        <View style={[styles.thumb, styles.thumbPlaceholder]}>
          <MaterialCommunityIcons name="image-off-outline" size={20} color="#9ca3af" />
        </View>
      )}

      <View style={{ flex: 1 }}>
        <View style={styles.rowTop}>
          <Text numberOfLines={1} style={styles.rowTitle}>{item.itemTitle}</Text>
          <View style={[styles.typeTag, tagStyle]}>
            <Text style={styles.typeTagText}>{tagText}</Text>
          </View>
        </View>

        {item.itemDescription ? <Text numberOfLines={1} style={styles.rowDesc}>{item.itemDescription}</Text> : null}

        <View style={styles.rowMeta}>
          <View style={styles.metaChip}>
            <MaterialCommunityIcons name="calendar-range" size={14} color="#6b7280" />
            <Text style={styles.metaText}>{due}</Text>
          </View>
          <View style={styles.metaChip}>
            <MaterialCommunityIcons name="timer-sand" size={14} color="#6b7280" />
            <Text style={styles.metaText}>D-{item.daysLeft}</Text>
          </View>
          {isMoney && (
            <View style={styles.metaChip}>
              <MaterialCommunityIcons name="cash" size={14} color="#6b7280" />
              <Text style={styles.metaText}>{item.amount?.toLocaleString()}원</Text>
            </View>
          )}
        </View>
      </View>
    </View>
  );
}

function OverDueRow({ item }: { item: OverDue }) {
  const isMoney = item.amount != null;
  const tagStyle = isMoney ? styles.moneyTag : styles.itemTag;
  const tagText = isMoney ? "금전대여" : "물품대여";
  const due = (item.dueAt || "").slice(0, 10);

  return (
    <View style={[styles.rowBox, { borderLeftWidth: 3, borderLeftColor: "#ef4444" }]}>
      {item.itemFileUrl ? (
        <Image source={{ uri: IMG_BASE_URL + item.itemFileUrl }} style={styles.thumb} />
      ) : (
        <View style={[styles.thumb, styles.thumbPlaceholder]}>
          <MaterialCommunityIcons name="image-off-outline" size={20} color="#9ca3af" />
        </View>
      )}

      <View style={{ flex: 1 }}>
        <View style={styles.rowTop}>
          <Text numberOfLines={1} style={styles.rowTitle}>{item.itemTitle}</Text>
          <View style={[styles.typeTag, tagStyle]}>
            <Text style={styles.typeTagText}>{tagText}</Text>
          </View>
        </View>

        {item.itemDescription ? <Text numberOfLines={1} style={styles.rowDesc}>{item.itemDescription}</Text> : null}

        <View style={styles.rowMeta}>
          <View style={styles.metaChip}>
            <MaterialCommunityIcons name="calendar-alert" size={14} color="#ef4444" />
            <Text style={[styles.metaText, { color: "#ef4444" }]}>{due}</Text>
          </View>
          <View style={styles.metaChip}>
            <MaterialCommunityIcons name="alert-circle-outline" size={14} color="#ef4444" />
            <Text style={[styles.metaText, { color: "#ef4444" }]}>D+{item.overDays}</Text>
          </View>
          {isMoney && (
            <View style={styles.metaChip}>
              <MaterialCommunityIcons name="cash" size={14} color="#6b7280" />
              <Text style={styles.metaText}>{item.amount?.toLocaleString()}원</Text>
            </View>
          )}
        </View>
      </View>
    </View>
  );
}

function PendingRow({ item }: { item: PendingItem }) {
  const titleUser = item.pendingUser ? `[${item.pendingUser}] 님의 ` : "";
  const statusText = item.status ? item.status : "요청을 보냈습니다";

  return (
    <View style={styles.pendingRow}>
      <View style={{ flex: 1 }}>
        <Text numberOfLines={1} style={styles.pendingTitle}>
          {titleUser}{statusText}
        </Text>
        {item.description ? (
          <Text numberOfLines={1} style={styles.pendingSub}>
            {item.description}
          </Text>
        ) : null}
      </View>

      <View style={{ alignItems: "flex-end" }}>
        {!!item.createdAt && (
          <Text style={styles.pendingTime}>{timeAgo(item.createdAt)}</Text>
        )}
        <MaterialCommunityIcons name="chevron-right" size={20} color="#9CA3AF" />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { padding: 16, gap: 16 },
  headerRow: { flexDirection: "row", alignItems: "center", justifyContent: "space-between" },
  greet: { fontWeight: "700" },

  statsRow: { flexDirection: "row", gap: 12 },
  statBox: { flex: 1, borderRadius: 14, borderWidth: 1, paddingVertical: 14, paddingHorizontal: 16, marginVertical: 10 },
  statRow: { flexDirection: "row", alignItems: "center" },
  statTitle: { fontSize: 13, marginBottom: 4 },
  statCount: { fontSize: 18, fontWeight: "700" },

  section: { gap: 10 },
  sectionHeader: { flexDirection: "row", alignItems: "center", justifyContent: "space-between", marginBottom: 10 },
  moreBtn: { color: "#2563eb", fontWeight: "700" },

  emptyBox: {
    paddingVertical: 24,
    alignItems: "center",
    backgroundColor: "#F9FAFB",
    borderRadius: 12,
    borderWidth: 1,
    borderColor: "#E5E7EB",
  },

  rowBox: {
    flexDirection: "row",
    gap: 12,
    padding: 12,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: "#E5E7EB",
    backgroundColor: "#FFFFFF",
  },
  thumb: { width: 56, height: 56, borderRadius: 8 },
  thumbPlaceholder: { justifyContent: "center", alignItems: "center", backgroundColor: "#F3F4F6" },

  rowTop: { flexDirection: "row", alignItems: "center", gap: 8 },
  rowTitle: { flex: 1, fontWeight: "700" },
  rowDesc: { color: "#6b7280", marginTop: 2 },

  typeTag: { paddingHorizontal: 10, paddingVertical: 3, borderRadius: 6, borderWidth: 1 },
  moneyTag: { backgroundColor: "#FF9800" },
  itemTag: { backgroundColor: "#5c36f4" },
  typeTagText: { fontSize: 11, fontWeight: "700", color: "#ffffffff" },

  rowMeta: { flexDirection: "row", alignItems: "center", gap: 10, marginTop: 6, flexWrap: "wrap" },
  metaChip: { flexDirection: "row", alignItems: "center", gap: 4, paddingVertical: 2 },
  metaText: { color: "#6b7280", fontSize: 12 },

  // 🔔 배지
  badge: {
    position: "absolute",
    top: -4,
    right: -6,
    minWidth: 18,
    height: 18,
    borderRadius: 9,
    backgroundColor: "#ef4444",
    alignItems: "center",
    justifyContent: "center",
    paddingHorizontal: 4,
  },
  badgeText: { color: "#fff", fontSize: 11, fontWeight: "700" },

  // 모달(화이트톤)
  dialog: {
    backgroundColor: "#ffffff",
    borderRadius: 16,
    paddingBottom: 6,
    shadowColor: "#000",
    shadowOpacity: 0.08,
    shadowRadius: 16,
    shadowOffset: { width: 0, height: 8 },
    elevation: 6,
  },
  dialogTitleRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: 6,
  },
  dialogTitleText: {
    fontWeight: "700",
    color: "#111827",
    fontSize: 18,
  },
  dialogContent: {
    backgroundColor: "#ffffff",
    paddingTop: 8,
  },
  dialogActions: {
    justifyContent: "flex-end",
    paddingHorizontal: 12,
  },

  pendingRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: 12,
    padding: 14,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: "#F3F4F6",
    backgroundColor: "#FFFFFF",
  },
  pendingTitle: {
    fontWeight: "700",
    fontSize: 14,
    color: "#111827",
  },
  pendingSub: {
    color: "#6b7280",
    marginTop: 2,
    fontSize: 12,
  },
  pendingTime: {
    color: "#9CA3AF",
    fontSize: 12,
    marginBottom: 2,
  },

  center: { flex: 1, justifyContent: "center", alignItems: "center" },
});
