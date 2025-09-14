import { MaterialCommunityIcons } from "@expo/vector-icons";
import { useNavigation } from "@react-navigation/native";
import React, { useCallback, useEffect, useState } from "react";
import {
  FlatList,
  ScrollView,
  StyleSheet,
  TouchableOpacity,
  View,
  Alert,
} from "react-native";
import {
  ActivityIndicator,
  Button,
  Dialog,
  Portal,
  Text,
  TextInput,
} from "react-native-paper";
import { SafeAreaView } from "react-native-safe-area-context";

import ComingReturnRow from "./components/ComingReturnRow";
import OverDueRow from "./components/OverDueRow";
import PendingRow from "./components/PendingRow";
import StatBox from "./components/StatBox";
import useHomeScreen from "./hooks/useHomeScreen";
import { updateProfile } from "../../api/users";

export default function HomeScreen() {
  const navigation = useNavigation<any>();

  const goToAgreement = useCallback(
    (agreementId: number) => {
      navigation.navigate("AgreementList", {
        screen: "MyAgreementDetail",
        params: { agreementId },
      });
    },
    [navigation]
  );

  const {
    name,
    phone,
    borrowedCount,
    lentCount,
    pendingCount,
    visibleComing,
    visibleOverdues,
    showAllComing,
    setShowAllComing,
    showAllOverdue,
    setShowAllOverdue,
    isLoading,
    error,
    pendingOpen,
    setPendingOpen,
    pendings,
    pendingHasNext,
    pendingLoading,
    loadPendings,
    openPending,
    refetch,
  } = useHomeScreen();

  const [phoneModalOpen, setPhoneModalOpen] = useState(false);
  const [newPhone, setNewPhone] = useState("");
  const [submittingPhone, setSubmittingPhone] = useState(false);

  useEffect(() => {
    if (!isLoading && !error && (!phone || phone.trim() === "")) {
      setPhoneModalOpen(true);
    }
  }, [isLoading, error, phone]);

  const handlePhoneSubmit = async () => {
    if (!newPhone || newPhone.length < 10 || newPhone.length > 11) {
      Alert.alert("안내", "휴대폰 번호를 10~11자리 숫자로 입력해주세요.");
      return;
    }
    try {
      setSubmittingPhone(true);
      await updateProfile({ phone: newPhone });
      Alert.alert("완료", "전화번호가 등록되었습니다.");
      setPhoneModalOpen(false);
      refetch();
    } catch (e: any) {
      const msg =
        e?.response?.data?.message ||
        e?.message ||
        "전화번호 등록에 실패했습니다.";
      Alert.alert("오류", msg);
    } finally {
      setSubmittingPhone(false);
    }
  };

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

  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: "#fff" }}>
      <ScrollView contentContainerStyle={styles.container}>
        {/* 헤더 */}
        <View style={styles.headerRow}>
          <Text variant="titleLarge" style={styles.greet}>
            안녕하세요{name ? `, ${name}` : ""}님!
          </Text>
          <TouchableOpacity
            onPress={openPending}
            hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
          >
            <View style={{ position: "relative" }}>
              <MaterialCommunityIcons
                name="bell-outline"
                size={22}
                color="#6b7280"
              />
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
          <StatBox title="빌려준 대여건" count={lentCount} tone="orange" />
          <StatBox title="빌린 대여건" count={borrowedCount} tone="blue" />
        </View>

        {/* 반납 임박 */}
        <View style={styles.section}>
          <View style={styles.sectionHeader}>
            <Text variant="titleMedium" style={{ fontWeight: "700" }}>
              반납일이 얼마 안 남았어요!
            </Text>
            <View
              style={{ flexDirection: "row", alignItems: "center", gap: 12 }}
            >
              {visibleComing.length >= 2 && (
                <TouchableOpacity onPress={() => setShowAllComing((v) => !v)}>
                  <Text style={styles.moreBtn}>
                    {showAllComing ? "간략히" : "더보기"}
                  </Text>
                </TouchableOpacity>
              )}
            </View>
          </View>

          {visibleComing.length === 0 ? (
            <View style={styles.emptyBox}>
              <Text style={{ color: "#6b7280" }}>
                반납 예정 항목이 없습니다.
              </Text>
            </View>
          ) : (
            visibleComing.map((it) => (
              <TouchableOpacity
                key={it.id}
                activeOpacity={0.7}
                onPress={() => goToAgreement(it.id)}
              >
                <ComingReturnRow item={it} />
              </TouchableOpacity>
            ))
          )}
        </View>

        {/* 연체 중 */}
        <View style={styles.section}>
          <View style={styles.sectionHeader}>
            <Text
              variant="titleMedium"
              style={{ fontWeight: "700", marginTop: 10 }}
            >
              연체 중! 약속한 날짜가 지났어요
            </Text>
            <View
              style={{ flexDirection: "row", alignItems: "center", gap: 12 }}
            >
              {visibleOverdues.length >= 2 && (
                <TouchableOpacity onPress={() => setShowAllOverdue((v) => !v)}>
                  <Text style={styles.moreBtn}>
                    {showAllOverdue ? "간략히" : "더보기"}
                  </Text>
                </TouchableOpacity>
              )}
            </View>
          </View>

          {visibleOverdues.length === 0 ? (
            <View style={styles.emptyBox}>
              <Text style={{ color: "#6b7280" }}>연체된 항목이 없습니다.</Text>
            </View>
          ) : (
            visibleOverdues.map((it) => (
              <TouchableOpacity
                key={it.id}
                activeOpacity={0.7}
                onPress={() => goToAgreement(it.id)}
              >
                <OverDueRow item={it} />
              </TouchableOpacity>
            ))
          )}
        </View>
      </ScrollView>

      {/* 승인 필요 모달 */}
      <Portal>
        <Dialog
          visible={pendingOpen}
          onDismiss={() => setPendingOpen(false)}
          style={styles.dialog}
        >
          <Dialog.Title>
            <View style={styles.dialogTitleRow}>
              <MaterialCommunityIcons
                name="bell-outline"
                size={18}
                color="#111827"
              />
              <Text style={styles.dialogTitleText}>승인 필요</Text>
            </View>
          </Dialog.Title>

          <Dialog.Content style={styles.dialogContent}>
            {pendings.length === 0 && !pendingLoading ? (
              <View style={[styles.emptyBox, { marginTop: 8 }]}>
                <Text style={{ color: "#6b7280" }}>
                  승인할 요청이 없습니다.
                </Text>
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
            <Button onPress={() => setPendingOpen(false)} textColor="#111827">
              닫기
            </Button>
          </Dialog.Actions>
        </Dialog>
      </Portal>

      {/* 전화번호 등록 모달 */}
      <Portal>
        <Dialog
          visible={phoneModalOpen}
          dismissable={false}
          style={styles.dialog}
        >
          <Dialog.Title>
            <Text style={styles.dialogTitleText}>전화번호 등록</Text>
          </Dialog.Title>
          <Dialog.Content style={styles.dialogContent}>
            <TextInput
              label="휴대폰 번호 (숫자만)"
              value={newPhone}
              onChangeText={(t) => setNewPhone(t.replace(/[^\d]/g, ""))}
              keyboardType="number-pad"
              left={<TextInput.Icon icon="cellphone" />}
              style={{ backgroundColor: "#fff" }}
              returnKeyType="done"
              onSubmitEditing={handlePhoneSubmit}
              blurOnSubmit
            />
          </Dialog.Content>
          <Dialog.Actions style={styles.dialogActions}>
            <Button
              onPress={handlePhoneSubmit}
              loading={submittingPhone}
              disabled={submittingPhone}
            >
              확인
            </Button>
          </Dialog.Actions>
        </Dialog>
      </Portal>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { padding: 16, gap: 16 },
  headerRow: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
  },
  greet: { fontWeight: "700" },
  statsRow: { flexDirection: "row", gap: 12 },
  section: { gap: 10 },
  sectionHeader: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    marginBottom: 10,
  },
  moreBtn: { color: "#2563eb", fontWeight: "700" },
  emptyBox: {
    paddingVertical: 24,
    alignItems: "center",
    backgroundColor: "#F9FAFB",
    borderRadius: 12,
    borderWidth: 1,
    borderColor: "#E5E7EB",
  },
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
  dialogTitleRow: { flexDirection: "row", alignItems: "center", gap: 6 },
  dialogTitleText: { fontWeight: "700", color: "#111827", fontSize: 18 },
  dialogContent: { backgroundColor: "#ffffff", paddingTop: 8 },
  dialogActions: { justifyContent: "flex-end", paddingHorizontal: 12 },
  center: { flex: 1, justifyContent: "center", alignItems: "center" },
});
