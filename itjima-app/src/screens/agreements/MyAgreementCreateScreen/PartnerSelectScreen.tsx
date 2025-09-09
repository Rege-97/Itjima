import { MaterialCommunityIcons } from "@expo/vector-icons";
import * as Contacts from "expo-contacts";
import React, { useEffect, useState } from "react";
import {
    FlatList,
    SafeAreaView,
    ScrollView,
    StyleSheet,
    TouchableOpacity,
    View,
} from "react-native";
import {
    ActivityIndicator,
    Button,
    Dialog,
    Divider,
    List,
    Portal,
    Text,
    TextInput,
} from "react-native-paper";
import { recentPartnersApi, searchUserByPhoneApi } from "../../../api/users";

type User = { id: number; name: string; phone: string };

const normalizePhone = (raw: string) => {
  if (!raw) return "";
  let phone = raw.replace(/[^0-9+]/g, "");
  if (phone.startsWith("+82")) phone = "0" + phone.slice(3);
  return phone;
};

export default function PartnerSelectScreen({ navigation }: any) {
  const [searchPhone, setSearchPhone] = useState("");
  const [isSearching, setIsSearching] = useState(false);
  const [searchResult, setSearchResult] = useState<User | null>(null);

  const [contacts, setContacts] = useState<any[]>([]);
  const [contactSearch, setContactSearch] = useState("");
  const [contactsLoaded, setContactsLoaded] = useState(false);

  const [partners, setPartners] = useState<User[]>([]);
  const [isLoadingPartners, setIsLoadingPartners] = useState(false);
  const [hasNextPartners, setHasNextPartners] = useState(true);
  const [lastPartnerId, setLastPartnerId] = useState<number | null>(null);

  const [isSearchModalVisible, setIsSearchModalVisible] = useState(false);
  const [isContactsVisible, setIsContactsVisible] = useState(false);
  const [isRecentVisible, setIsRecentVisible] = useState(false);

  const goNext = (user: User) => {
    navigation.navigate("MyAgreementCreate", { debtorUser: user });
  };

  const handleSearch = async (phone?: string) => {
    const target = phone || searchPhone;
    if (!target) return;
    setIsSearching(true);
    try {
      const res = await searchUserByPhoneApi(normalizePhone(target));
      const user = res?.data?.data as User | undefined;
      setSearchResult(user ?? null);
    } catch {
      setSearchResult(null);
    } finally {
      setIsSearching(false);
    }
  };

  const loadContacts = async () => {
    const { status } = await Contacts.requestPermissionsAsync();
    if (status !== "granted") return;
    const { data } = await Contacts.getContactsAsync({
      fields: [Contacts.Fields.PhoneNumbers],
    });
    setContacts(data);
    setContactsLoaded(true);
  };

  const filteredContacts = contacts.filter((c) =>
    (c.name || "").toLowerCase().includes(contactSearch.toLowerCase())
  );

  const loadPartners = async (reset?: boolean) => {
    if (isLoadingPartners) return;
    if (!hasNextPartners && !reset) return;

    setIsLoadingPartners(true);
    try {
      const cursor = reset ? undefined : lastPartnerId ?? undefined;
      const res = await recentPartnersApi(cursor);
      const payload = res.data?.data;

      setPartners((prev) =>
        reset ? payload?.items || [] : [...prev, ...(payload?.items || [])]
      );
      setHasNextPartners(!!payload?.hasNext);
      setLastPartnerId(payload?.lastId ?? null);
    } finally {
      setIsLoadingPartners(false);
    }
  };

  useEffect(() => {
    loadPartners();
  }, []);

  return (
    <SafeAreaView style={styles.safe}>
      <ScrollView contentContainerStyle={styles.container}>
        <View style={styles.pageHeader}>
          <View style={styles.pageHeaderIcon}>
            <MaterialCommunityIcons
              name="package-variant-closed"
              size={30}
              color="#2F3438"
            />
          </View>
          <View style={{ flex: 1 }}>
            <Text style={styles.pageHeaderTitle}>대여 등록</Text>
            <Text style={styles.pageHeaderSubtitle}>
              물건이나 금전을 대여 요청을 보냅니다.
            </Text>
          </View>
        </View>
        <Divider style={styles.topDivider} />
        <View style={styles.sectionHeaderRow}>
          <MaterialCommunityIcons
            name="account-outline"
            size={22}
            color="#111"
            style={{ marginRight: 8 }}
          />
          <Text style={styles.sectionHeaderTitle}>대여 상대방 선택</Text>
        </View>
        <TouchableOpacity
          onPress={() => {
            setIsSearchModalVisible(true);
            setTimeout(() => setSearchResult(null), 0);
          }}
          style={[styles.menuCard, styles.menuMuted]}
          activeOpacity={0.9}
        >
          <MaterialCommunityIcons name="magnify" size={24} color="#5B6166" />
          <Text style={styles.menuTitle}>번호로 검색</Text>
          <Text style={styles.menuDesc}>전화번호로 사용자 찾기</Text>
        </TouchableOpacity>

        <TouchableOpacity
          onPress={async () => {
            if (!contactsLoaded) await loadContacts();
            setContactSearch("");
            setIsContactsVisible(true);
          }}
          style={styles.menuCard}
          activeOpacity={0.9}
        >
          <MaterialCommunityIcons name="phone" size={22} color="#5B6166" />
          <Text style={styles.menuTitle}>연락처 선택</Text>
          <Text style={styles.menuDesc}>저장된 연락처에서 선택</Text>
        </TouchableOpacity>

        <TouchableOpacity
          onPress={async () => {
            setPartners([]);
            setLastPartnerId(null);
            setHasNextPartners(true);
            setIsRecentVisible(true);
            await loadPartners(true);
          }}
          style={styles.menuCard}
          activeOpacity={0.9}
        >
          <MaterialCommunityIcons
            name="account-clock"
            size={22}
            color="#5B6166"
          />
          <Text style={styles.menuTitle}>최근 거래자</Text>
          <Text style={styles.menuDesc}>최근 거래한 사용자</Text>
        </TouchableOpacity>
      </ScrollView>

      {/* 모달들 */}
      <Portal>
        {/* 번호 검색 모달 */}
        <Dialog
          visible={isSearchModalVisible}
          onDismiss={() => setIsSearchModalVisible(false)}
          style={styles.dialog}
        >
          <Dialog.Title style={styles.dialogTitle}>사용자 찾기</Dialog.Title>
          <Dialog.Content style={styles.dialogContent}>
            <TextInput
              mode="outlined"
              label="휴대폰 번호"
              value={searchPhone}
              onChangeText={setSearchPhone}
              keyboardType="phone-pad"
              style={styles.input}
            />
            <Button
              mode="contained"
              onPress={() => handleSearch()}
              loading={isSearching}
              style={styles.dialogBtn}
            >
              번호로 검색
            </Button>
            <Button
              mode="outlined"
              onPress={async () => {
                if (!contactsLoaded) await loadContacts();
                setIsContactsVisible(true);
              }}
              style={styles.dialogBtn}
            >
              연락처에서 선택
            </Button>
            <Button
              mode="outlined"
              onPress={async () => {
                setPartners([]);
                setLastPartnerId(null);
                setHasNextPartners(true);
                setIsRecentVisible(true);
                await loadPartners();
              }}
              style={styles.dialogBtn}
            >
              최근 거래한 사용자
            </Button>

            {searchResult && (
              <View style={{ marginTop: 16 }}>
                <Text>이름: {searchResult.name}</Text>
                <Text>전화번호: {searchResult.phone}</Text>
                <Button
                  mode="outlined"
                  onPress={() => {
                    setIsSearchModalVisible(false);
                    goNext(searchResult);
                  }}
                  style={styles.dialogBtn}
                >
                  선택하기
                </Button>
              </View>
            )}
          </Dialog.Content>
        </Dialog>

        {/* 연락처 모달 */}
        <Dialog
          visible={isContactsVisible}
          onDismiss={() => setIsContactsVisible(false)}
          style={styles.dialog}
        >
          <Dialog.Title style={styles.dialogTitle}>연락처 선택</Dialog.Title>
          <Dialog.Content style={[styles.dialogContent, { height: 360 }]}>
            <TextInput
              mode="outlined"
              label="이름 검색"
              value={contactSearch}
              onChangeText={setContactSearch}
              style={styles.input}
            />
            <FlatList
              data={filteredContacts}
              keyExtractor={(item) => item.id}
              keyboardShouldPersistTaps="handled"
              ItemSeparatorComponent={Divider}
              renderItem={({ item }) => {
                const phone = normalizePhone(
                  item.phoneNumbers?.[0]?.number || ""
                );
                return (
                  <List.Item
                    title={item.name}
                    description={phone}
                    left={(p) => <List.Icon {...p} icon="account" />}
                    right={(p) => <List.Icon {...p} icon="chevron-right" />}
                    onPress={() => {
                      setIsContactsVisible(false);
                      setIsSearchModalVisible(true);
                      handleSearch(phone);
                    }}
                    style={styles.listRow}
                  />
                );
              }}
            />
          </Dialog.Content>
        </Dialog>

        {/* 최근 거래자 모달 */}
        <Dialog
          visible={isRecentVisible}
          onDismiss={() => setIsRecentVisible(false)}
          style={styles.dialog}
        >
          <Dialog.Title style={styles.dialogTitle}>
            최근 거래한 사용자
          </Dialog.Title>
          <Dialog.Content style={[styles.dialogContent, { height: 360 }]}>
            <FlatList
              data={partners}
              keyExtractor={(item) => item.id.toString()}
              onEndReached={() => loadPartners()}
              onEndReachedThreshold={0.5}
              ItemSeparatorComponent={Divider}
              ListFooterComponent={
                isLoadingPartners ? (
                  <ActivityIndicator style={{ margin: 10 }} />
                ) : null
              }
              renderItem={({ item }) => (
                <List.Item
                  title={item.name}
                  description={item.phone}
                  left={(p) => <List.Icon {...p} icon="account" />}
                  right={(p) => <List.Icon {...p} icon="chevron-right" />}
                  onPress={() => {
                    setIsRecentVisible(false);
                    goNext(item);
                  }}
                  style={styles.listRow}
                />
              )}
            />
          </Dialog.Content>
        </Dialog>
      </Portal>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safe: {
    flex: 1,
    backgroundColor: "#ffffffff",
  },
  container: {
    paddingVertical: 8,
    paddingHorizontal: 12,
  },
  pageHeader: {
    flexDirection: "row",
    alignItems: "center",
    paddingVertical: 6,
    marginVertical: 16,
  },
  pageHeaderIcon: {
    width: 50,
    height: 50,
    borderRadius: 30,
    backgroundColor: "#E9ECEF",
    alignItems: "center",
    justifyContent: "center",
    marginRight: 20,
  },
  pageHeaderIconText: {
    fontWeight: "700",
    color: "#2F3438",
  },
  pageHeaderTitle: {
    fontSize: 25,
    fontWeight: "800",
    color: "#111",
  },
  pageHeaderSubtitle: {
    fontSize: 14,
    color: "#6B7280",
    marginTop: 5,
  },
  topDivider: {
    marginTop: 10,
    marginBottom: 24,
  },
  sectionHeaderRow: {
    flexDirection: "row",
    alignItems: "center",
    marginBottom: 25,
    paddingLeft: 2,
  },
  sectionHeaderTitle: {
    fontSize: 20,
    fontWeight: "800",
    color: "#111",
  },
  menuCard: {
    paddingVertical: 20,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: "#E6E6E6",
    backgroundColor: "#FFFFFF",
    alignItems: "center",
    marginBottom: 20,
    elevation: 1,
  },
  menuMuted: {
    backgroundColor: "#F7F7F7",
  },
  menuTitle: {
    fontWeight: "700",
    marginTop: 8,
    color: "#2F3438",
  },
  menuDesc: {
    color: "#7B848D",
    marginTop: 4,
    fontSize: 12,
  },
  dialog: {
    backgroundColor: "#FFFFFF",
    borderRadius: 18,
  },
  dialogTitle: {
    fontWeight: "700",
  },
  dialogContent: {
    backgroundColor: "#FFFFFF",
    borderRadius: 14,
    paddingTop: 6,
  },
  dialogBtn: {
    marginTop: 10,
  },
  listRow: {
    paddingVertical: 10,
  },
  input: {
    marginBottom: 20,
    height: 60,
    backgroundColor: "transparent",
  },
});
