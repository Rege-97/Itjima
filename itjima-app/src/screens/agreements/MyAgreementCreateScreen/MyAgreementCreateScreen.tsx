import React, { useCallback, useState } from "react";
import {
  Alert,
  SafeAreaView,
  View,
  FlatList,
  TouchableOpacity,
  Image,
  Keyboard,
  KeyboardAvoidingView,
  Platform,
  TouchableWithoutFeedback,
  ScrollView,
} from "react-native";
import {
  ActivityIndicator,
  Button,
  Dialog,
  Portal,
  SegmentedButtons,
  Text,
  TextInput,
} from "react-native-paper";
import * as Contacts from "expo-contacts";
import * as ImagePicker from "expo-image-picker";
import * as ImageManipulator from "expo-image-manipulator";
import { DatePickerModal } from "react-native-paper-dates";
import { recentPartnersApi, searchUserByPhoneApi } from "../../../api/users";
import { useItem } from "../../items/MyItemScreen/hooks/useItem";
import { IMG_BASE_URL } from "@env";
import { agreementCreateApi } from "../../../api/agreements";
import { createItemApi, updateItemImageApi } from "../../../api/items";
import { useFocusEffect } from "@react-navigation/native";

const MAX_DIM = 1280;

type User = {
  id: number;
  name: string;
  phone: string;
};
type RentType = "ITEM" | "MONEY";
type RentSource = "EXISTING" | "NEW";

const normalizePhone = (raw: string) => {
  if (!raw) return "";
  let phone = raw.replace(/[^0-9+]/g, "");
  if (phone.startsWith("+82")) {
    phone = "0" + phone.slice(3);
  }
  return phone;
};

const MyAgreementCreateScreen = () => {
  const [debtorUser, setDebtorUser] = useState<User | null>(null);

  // 모달 상태
  const [isSearchModalVisible, setIsSearchModalVisible] = useState(false);
  const [searchPhone, setSearchPhone] = useState("");
  const [searchResult, setSearchResult] = useState<User | null>(null);
  const [isSearching, setIsSearching] = useState(false);
  const [isRecentVisible, setIsRecentVisible] = useState(false);

  // 연락처
  const [contacts, setContacts] = useState<any[]>([]);
  const [isContactsVisible, setIsContactsVisible] = useState(false);
  const [contactSearch, setContactSearch] = useState("");

  // 최근 파트너
  const [partners, setPartners] = useState<User[]>([]);
  const [isLoadingPartners, setIsLoadingPartners] = useState(false);
  const [hasNextPartners, setHasNextPartners] = useState(true);
  const [lastPartnerId, setLastPartnerId] = useState<number | null>(null);

  // 대여 타입 & 소스
  const [rentType, setRentType] = useState<RentType | null>(null);
  const [rentSource, setRentSource] = useState<RentSource | null>(null);
  const [amount, setAmount] = useState("");

  // 새 물건 등록 상태
  const [itemTitle, setItemTitle] = useState("");
  const [itemDesc, setItemDesc] = useState("");
  const [itemImage, setItemImage] = useState<{
    uri: string;
    name: string;
    type: string;
  } | null>(null);

  // 기존 물건 상태
  const [itemId, setItemId] = useState<number | null>(null);
  const [selectedItem, setSelectedItem] = useState<any | null>(null);

  // 반납일 상태
  const [dueDate, setDueDate] = useState<Date | undefined>(undefined);
  const [isDateModalVisible, setIsDateModalVisible] = useState(false);

  // 대여 상세 (terms)
  const [terms, setTerms] = useState("");

  const {
    items,
    isLoading,
    isRefreshing,
    fetchMoreItems,
    onRefresh,
    searchQuery,
    setSearchQuery,
    handleSearchSubmit,
  } = useItem(null);

  const [isItemModalVisible, setIsItemModalVisible] = useState(false);

  const handleCreate = async () => {
    if (!debtorUser) {
      Alert.alert("알림", "대여 상대방을 선택해주세요.");
      return;
    }
    if (!dueDate) {
      Alert.alert("알림", "반납일을 선택해주세요.");
      return;
    }

    try {
      let finalItemId = itemId;

      // 새 물건 등록
      if (rentType === "ITEM" && rentSource === "NEW") {
        if (!itemTitle.trim()) {
          Alert.alert("알림", "물건 제목을 입력해주세요.");
          return;
        }

        const res = await createItemApi({
          type: "OBJECT",
          title: itemTitle,
          description: itemDesc,
        });

        finalItemId = res.data.data.id;

        // 이미지 있으면 업로드
        if (itemImage) {
          const formData = new FormData();
          formData.append("img", {
            uri: itemImage.uri,
            name: itemImage.name,
            type: itemImage.type,
          } as any);
          await updateItemImageApi(finalItemId!, formData);
        }
      }

      // 금전 대여
      if (rentType === "MONEY") {
        if (!amount.trim()) {
          Alert.alert("알림", "금액을 입력해주세요.");
          return;
        }

        const res = await createItemApi({
          type: "MONEY",
          title: "금전 대여",
          description: `${amount}원`,
        });

        finalItemId = res.data.data.id;
      }

      // 대여 등록
      if (!finalItemId) {
        Alert.alert("알림", "대여할 아이템을 선택해주세요.");
        return;
      }

      await agreementCreateApi({
        itemId: finalItemId,
        amount: rentType === "MONEY" ? amount : undefined,
        dueAt: dueDate.toISOString(),
        terms,
        debtorUserId: debtorUser.id,
      });

      Alert.alert("성공", "대여가 등록되었습니다.");
      // navigation.goBack(); 필요하면 추가
    } catch (err) {
      console.error("대여 등록 실패:", err);
      Alert.alert("오류", "대여 등록에 실패했습니다.");
    }
  };

  useFocusEffect(
    useCallback(() => {
      setDebtorUser(null);
      setSearchPhone("");
      setSearchResult(null);
      setIsSearching(false);
      setIsSearchModalVisible(false);
      setIsRecentVisible(false);
      setContacts([]);
      setIsContactsVisible(false);
      setContactSearch("");

      setPartners([]);
      setIsLoadingPartners(false);
      setHasNextPartners(true);
      setLastPartnerId(null);

      setRentType(null);
      setRentSource(null);
      setAmount("");

      setItemTitle("");
      setItemDesc("");
      setItemImage(null);
      setItemId(null);
      setSelectedItem(null);

      setDueDate(undefined);
      setIsDateModalVisible(false);

      setTerms("");
    }, [])
  );

  // 이미지 선택
  const pickImage = async () => {
    const { status } = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (status !== "granted") {
      Alert.alert(
        "권한 필요",
        "이미지를 업로드하려면 사진 라이브러리 접근 권한이 필요합니다."
      );
      return;
    }
    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      allowsEditing: true,
      aspect: [4, 3],
      quality: 1,
    });

    if (result.canceled) return;

    const asset = result.assets[0];
    const { uri, width, height } = asset;

    const scale = Math.min(
      1,
      MAX_DIM / Math.max(width ?? MAX_DIM, height ?? MAX_DIM)
    );
    const targetW = Math.round((width ?? MAX_DIM) * scale);
    const targetH = Math.round((height ?? MAX_DIM) * scale);

    const manipulated = await ImageManipulator.manipulateAsync(
      uri,
      [{ resize: { width: targetW, height: targetH } }],
      { compress: 0.7, format: ImageManipulator.SaveFormat.JPEG }
    );

    const filename = (
      asset.fileName ||
      uri.split("/").pop() ||
      "image"
    ).replace(/\.[^/.]+$/, "");
    const finalName = `${filename}.jpg`;

    setItemImage({
      uri: manipulated.uri,
      name: finalName,
      type: "image/jpeg",
    });
  };

  // ✅ 사용자 검색
  const handleSearch = async (phone?: string) => {
    const targetPhone = phone || searchPhone;
    if (!targetPhone) {
      Alert.alert("알림", "전화번호를 입력해주세요.");
      return;
    }
    setIsSearching(true);
    try {
      const res = await searchUserByPhoneApi(normalizePhone(targetPhone));
      if (!res.data?.data) {
        Alert.alert("알림", "가입하지 않은 유저입니다.");
        setSearchResult(null);
        return;
      }
      setSearchResult(res.data.data);
    } catch (err) {
      console.error("검색 실패:", err);
      Alert.alert("알림", "가입하지 않은 유저입니다.");
      setSearchResult(null);
    } finally {
      setIsSearching(false);
    }
  };

  // 연락처 불러오기
  const loadContacts = async () => {
    const { status } = await Contacts.requestPermissionsAsync();
    if (status !== "granted") {
      Alert.alert("권한 필요", "연락처 접근 권한이 필요합니다.");
      return;
    }
    const { data } = await Contacts.getContactsAsync({
      fields: [Contacts.Fields.PhoneNumbers],
    });
    setContacts(data);
    setContactSearch("");
    setIsContactsVisible(true);
  };

  // ✅ 최근 파트너 불러오기
  const loadPartners = async () => {
    if (isLoadingPartners || !hasNextPartners) return;
    setIsLoadingPartners(true);
    try {
      const res = await recentPartnersApi(lastPartnerId!);
      const fetchedData = res.data.data;
      setPartners((prev) => [...prev, ...(fetchedData.items || [])]);
      setHasNextPartners(fetchedData.hasNext);
      setLastPartnerId(fetchedData.lastId);
    } catch (err) {
      console.error("최근 파트너 불러오기 실패:", err);
    } finally {
      setIsLoadingPartners(false);
    }
  };

  // ✅ 사용자 선택 시 초기화
  const handleSelectUser = (user: User) => {
    setDebtorUser(user);
    setItemId(null);
    setSelectedItem(null);
    setItemTitle("");
    setItemDesc("");
    setItemImage(null);
    setAmount("");
    setRentSource(null);
    setTerms("");
  };

  // ✅ rentType 변경 시 초기화
  const handleRentTypeChange = (v: string) => {
    const newType = v as RentType;
    setRentType(newType);

    if (newType === "ITEM") {
      setAmount("");
    } else if (newType === "MONEY") {
      setItemId(null);
      setSelectedItem(null);
      setItemTitle("");
      setItemDesc("");
      setItemImage(null);
      setRentSource(null);
    }
  };

  // ✅ rentSource 변경 시 초기화
  const handleRentSourceChange = (v: string) => {
    const newSource = v as RentSource;
    setRentSource(newSource);

    if (newSource === "EXISTING") {
      setItemTitle("");
      setItemDesc("");
      setItemImage(null);
    } else if (newSource === "NEW") {
      setItemId(null);
      setSelectedItem(null);
    }
  };

  // 연락처 검색 필터링
  const filteredContacts = contacts.filter((c) =>
    c.name?.toLowerCase().includes(contactSearch.toLowerCase())
  );

  return (
    <>
      {/* ✅ 모달들은 TouchableWithoutFeedback 바깥 */}
      <Portal>
        {/* 사용자 검색 모달 */}
        <Dialog
          visible={isSearchModalVisible}
          onDismiss={() => setIsSearchModalVisible(false)}
        >
          <Dialog.Title>사용자 찾기</Dialog.Title>
          <Dialog.Content>
            <TextInput
              label="휴대폰 번호"
              value={searchPhone}
              onChangeText={setSearchPhone}
              keyboardType="phone-pad"
            />
            <Button
              mode="contained"
              onPress={() => handleSearch()}
              loading={isSearching}
              style={{ marginTop: 10 }}
            >
              번호로 검색
            </Button>

            <Button
              mode="outlined"
              onPress={loadContacts}
              style={{ marginTop: 10 }}
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
              style={{ marginTop: 10 }}
            >
              최근 거래한 사용자
            </Button>

            {searchResult && (
              <View style={{ marginTop: 20 }}>
                <Text>이름: {searchResult.name}</Text>
                <Text>전화번호: {searchResult.phone}</Text>
                <Button
                  mode="outlined"
                  onPress={() => {
                    handleSelectUser(searchResult);
                    setIsSearchModalVisible(false);
                  }}
                  style={{ marginTop: 10 }}
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
        >
          <Dialog.Title>연락처 선택</Dialog.Title>
          <Dialog.Content style={{ height: 350 }}>
            <TextInput
              label="이름 검색"
              value={contactSearch}
              onChangeText={setContactSearch}
              style={{ marginBottom: 10 }}
            />
            <FlatList
              data={filteredContacts}
              keyExtractor={(item) => item.id}
              keyboardShouldPersistTaps="handled"
              renderItem={({ item }) => {
                const phone = normalizePhone(
                  item.phoneNumbers?.[0]?.number || ""
                );
                return (
                  <TouchableOpacity
                    onPress={() => {
                      setIsContactsVisible(false);
                      handleSearch(phone);
                    }}
                    style={{ paddingVertical: 8 }}
                  >
                    <Text>{item.name}</Text>
                    <Text>{phone}</Text>
                  </TouchableOpacity>
                );
              }}
            />
          </Dialog.Content>
        </Dialog>

        {/* 기존 물건 모달 */}
        <Dialog
          visible={isItemModalVisible}
          onDismiss={() => setIsItemModalVisible(false)}
        >
          <Dialog.Title>대여 가능한 물건 선택</Dialog.Title>
          <Dialog.Content style={{ height: 400 }}>
            <TextInput
              label="물건 이름 검색"
              value={searchQuery}
              onChangeText={setSearchQuery}
              onSubmitEditing={handleSearchSubmit}
              style={{ marginBottom: 10 }}
            />
            <FlatList
              data={items}
              keyExtractor={(item) => item.id.toString()}
              refreshing={isRefreshing}
              onRefresh={onRefresh}
              onEndReached={fetchMoreItems}
              onEndReachedThreshold={0.5}
              keyboardShouldPersistTaps="handled"
              renderItem={({ item }) => (
                <TouchableOpacity
                  onPress={() => {
                    setItemId(item.id);
                    setSelectedItem(item);
                    setIsItemModalVisible(false);
                  }}
                  style={{
                    flexDirection: "row",
                    alignItems: "center",
                    paddingVertical: 10,
                    borderBottomWidth: 0.5,
                    borderBottomColor: "#ccc",
                  }}
                >
                  <Image
                    source={{
                      uri: item.fileUrl
                        ? IMG_BASE_URL + item.fileUrl
                        : "https://via.placeholder.com/150",
                    }}
                    style={{
                      width: 60,
                      height: 60,
                      borderRadius: 8,
                      marginRight: 10,
                    }}
                  />
                  <View style={{ flex: 1 }}>
                    <Text style={{ fontWeight: "bold" }}>{item.title}</Text>
                    <Text numberOfLines={1} style={{ color: "#555" }}>
                      {item.description}
                    </Text>
                  </View>
                </TouchableOpacity>
              )}
            />
          </Dialog.Content>
        </Dialog>

        {/* 🤝 최근 파트너 모달 */}
        <Dialog
          visible={isRecentVisible}
          onDismiss={() => setIsRecentVisible(false)}
        >
          <Dialog.Title>최근 거래한 사용자</Dialog.Title>
          <Dialog.Content style={{ height: 350 }}>
            <FlatList
              data={partners}
              keyExtractor={(item) => item.id.toString()}
              onEndReached={loadPartners}
              onEndReachedThreshold={0.5}
              ListFooterComponent={
                isLoadingPartners ? (
                  <ActivityIndicator style={{ margin: 10 }} />
                ) : null
              }
              renderItem={({ item }) => (
                <TouchableOpacity
                  onPress={() => {
                    setDebtorUser(item);
                    setIsRecentVisible(false);
                    setIsSearchModalVisible(false);
                  }}
                  style={{
                    paddingVertical: 10,
                    borderBottomWidth: 0.5,
                    borderBottomColor: "#ccc",
                  }}
                >
                  <Text style={{ fontWeight: "bold" }}>{item.name}</Text>
                  <Text style={{ color: "#555" }}>{item.phone}</Text>
                </TouchableOpacity>
              )}
            />
          </Dialog.Content>
        </Dialog>

        {/* 반납일 모달 */}
        <DatePickerModal
          locale="ko"
          mode="single"
          visible={isDateModalVisible}
          onDismiss={() => setIsDateModalVisible(false)}
          date={dueDate}
          onConfirm={({ date }) => {
            setIsDateModalVisible(false);
            setDueDate(date);
          }}
          validRange={{ startDate: new Date() }}
        />
      </Portal>

      {/* ✅ 본문 */}
      <TouchableWithoutFeedback onPress={Keyboard.dismiss} accessible={false}>
        <KeyboardAvoidingView
          style={{ flex: 1 }}
          behavior={Platform.OS === "ios" ? "padding" : undefined}
        >
          <SafeAreaView style={{ flex: 1, padding: 16 }}>
            <ScrollView>
              {/* 사용자 선택 */}
              <Button
                mode="outlined"
                onPress={() => setIsSearchModalVisible(true)}
                style={{ marginVertical: 20 }}
              >
                사용자 검색 / 연락처 선택
              </Button>

              {debtorUser && (
                <View style={{ marginTop: 20 }}>
                  <Text>선택된 사용자</Text>
                  <Text>
                    {debtorUser.name} ({debtorUser.phone})
                  </Text>
                </View>
              )}

              {/* 대여 타입 */}
              <Text style={{ marginTop: 20, marginBottom: 10 }}>
                대여 타입 선택
              </Text>
              <SegmentedButtons
                value={rentType!}
                onValueChange={handleRentTypeChange}
                buttons={[
                  { value: "ITEM", label: "물건" },
                  { value: "MONEY", label: "금전" },
                ]}
              />

              {/* ITEM */}
              {rentType === "ITEM" && (
                <View style={{ marginTop: 20 }}>
                  <Text style={{ marginBottom: 10 }}>물건 선택 방식</Text>
                  <SegmentedButtons
                    value={rentSource!}
                    onValueChange={handleRentSourceChange}
                    buttons={[
                      { value: "EXISTING", label: "기존 물건" },
                      { value: "NEW", label: "새로 등록" },
                    ]}
                  />

                  {rentSource === "EXISTING" && (
                    <>
                      <Button
                        mode="outlined"
                        onPress={() => setIsItemModalVisible(true)}
                        style={{ marginTop: 20 }}
                      >
                        기존 물건 선택하기
                      </Button>
                      {selectedItem && (
                        <View
                          style={{
                            flexDirection: "row",
                            alignItems: "center",
                            marginTop: 20,
                            padding: 10,
                            borderWidth: 1,
                            borderColor: "#ccc",
                            borderRadius: 8,
                          }}
                        >
                          <Image
                            source={{
                              uri: selectedItem.fileUrl
                                ? IMG_BASE_URL + selectedItem.fileUrl
                                : "https://via.placeholder.com/150",
                            }}
                            style={{
                              width: 60,
                              height: 60,
                              borderRadius: 8,
                              marginRight: 10,
                            }}
                          />
                          <View style={{ flex: 1 }}>
                            <Text style={{ fontWeight: "bold" }}>
                              {selectedItem.title}
                            </Text>
                            <Text numberOfLines={2} style={{ color: "#555" }}>
                              {selectedItem.description}
                            </Text>
                          </View>
                        </View>
                      )}
                    </>
                  )}

                  {rentSource === "NEW" && (
                    <View style={{ marginTop: 20 }}>
                      <TextInput
                        label="물건 제목"
                        value={itemTitle}
                        onChangeText={setItemTitle}
                        style={{ marginBottom: 10 }}
                      />
                      <TextInput
                        label="물건 설명"
                        value={itemDesc}
                        onChangeText={setItemDesc}
                        multiline
                        style={{ marginBottom: 10 }}
                      />
                      <Button mode="outlined" onPress={pickImage}>
                        이미지 선택
                      </Button>
                      {itemImage && (
                        <Image
                          source={{ uri: itemImage.uri }}
                          style={{ width: "100%", height: 200, marginTop: 10 }}
                          resizeMode="cover"
                        />
                      )}
                    </View>
                  )}
                </View>
              )}

              {/* MONEY */}
              {rentType === "MONEY" && (
                <View style={{ marginTop: 20 }}>
                  <TextInput
                    label="대여 금액"
                    value={amount}
                    onChangeText={setAmount}
                    keyboardType="numeric"
                  />
                </View>
              )}

              {/* terms */}
              <View style={{ marginTop: 20 }}>
                <TextInput
                  label="대여 상세 내용"
                  value={terms}
                  onChangeText={setTerms}
                  multiline
                />
              </View>

              {/* 반납일 */}
              <Button
                mode="outlined"
                onPress={() => setIsDateModalVisible(true)}
                style={{ marginTop: 20 }}
              >
                {dueDate
                  ? `반납일: ${dueDate.toLocaleDateString()}`
                  : "반납일 선택"}
              </Button>
              <Button
                mode="contained"
                onPress={handleCreate}
                style={{ marginTop: 30 }}
              >
                대여 등록하기
              </Button>
            </ScrollView>
          </SafeAreaView>
        </KeyboardAvoidingView>
      </TouchableWithoutFeedback>
    </>
  );
};

export default MyAgreementCreateScreen;
