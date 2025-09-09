import { IMG_BASE_URL } from "@env";
import { MaterialCommunityIcons } from "@expo/vector-icons";
import { useHeaderHeight } from "@react-navigation/elements";
import { useFocusEffect } from "@react-navigation/native";
import * as ImageManipulator from "expo-image-manipulator";
import * as ImagePicker from "expo-image-picker";
import React, { useCallback, useRef, useState } from "react";
import {
    Alert,
    FlatList,
    Image,
    Keyboard,
    KeyboardAvoidingView,
    Platform,
    SafeAreaView,
    ScrollView,
    StyleSheet,
    TouchableOpacity,
    TouchableWithoutFeedback,
    View,
} from "react-native";
import {
    Dialog,
    Divider,
    List,
    Menu,
    Portal,
    Text,
    TextInput,
} from "react-native-paper";
import { DatePickerModal } from "react-native-paper-dates";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { agreementCreateApi } from "../../../api/agreements";
import { createItemApi, updateItemImageApi } from "../../../api/items";
import { useItem } from "../../items/MyItemScreen/hooks/useItem";

const MAX_DIM = 1280;
const AVAILABLE_STATUS = "AVAILABLE";

type User = { id: number; name: string; phone: string };
type RentType = "ITEM" | "MONEY";
type RentSource = "EXISTING" | "NEW";

export default function MyAgreementCreateScreen({ route, navigation }: any) {
  const { debtorUser: initialDebtor } = route.params;

  const [debtorUser, setDebtorUser] = useState<User | null>(initialDebtor);

  const [rentType, setRentType] = useState<RentType | null>(null);
  const [rentSource, setRentSource] = useState<RentSource | null>(null);
  const [amount, setAmount] = useState("");

  const [itemTitle, setItemTitle] = useState("");
  const [itemDesc, setItemDesc] = useState("");
  const [itemImage, setItemImage] = useState<{
    uri: string;
    name: string;
    type: string;
  } | null>(null);

  const [itemId, setItemId] = useState<number | null>(null);
  const [selectedItem, setSelectedItem] = useState<any | null>(null);

  const [dueDate, setDueDate] = useState<Date | undefined>(undefined);
  const [isDateModalVisible, setIsDateModalVisible] = useState(false);
  const [terms, setTerms] = useState("");

  const {
    items,
    isRefreshing,
    fetchMoreItems,
    onRefresh,
    searchQuery,
    setSearchQuery,
    handleSearchSubmit,
    handleFilterPress,
  } = useItem(null);
  const [isItemModalVisible, setIsItemModalVisible] = useState(false);

  const [rentTypeMenuOpen, setRentTypeMenuOpen] = useState(false);
  const [rentSourceMenuOpen, setRentSourceMenuOpen] = useState(false);

  const insets = useSafeAreaInsets();
  const headerHeight = useHeaderHeight();
  const keyboardOffset = headerHeight + insets.top + 8;
  const scrollRef = useRef<ScrollView>(null);

  useFocusEffect(
    useCallback(() => {
      setDebtorUser(initialDebtor);
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
    }, [initialDebtor])
  );

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
    setItemImage({
      uri: manipulated.uri,
      name: `${filename}.jpg`,
      type: "image/jpeg",
    });
  };

  const handleRentTypeChange = (v: string) => {
    const t = v as RentType;
    setRentType(t);
    if (t === "ITEM") setAmount("");
    if (t === "MONEY") {
      setItemId(null);
      setSelectedItem(null);
      setItemTitle("");
      setItemDesc("");
      setItemImage(null);
      setRentSource(null);
    }
  };
  const handleRentSourceChange = (v: string) => {
    const s = v as RentSource;
    setRentSource(s);
    if (s === "EXISTING") {
      setItemTitle("");
      setItemDesc("");
      setItemImage(null);
    }
    if (s === "NEW") {
      setItemId(null);
      setSelectedItem(null);
    }
  };

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
        if (itemImage) {
          const fd = new FormData();
          fd.append("img", {
            uri: itemImage.uri,
            name: itemImage.name,
            type: itemImage.type,
          } as any);
          await updateItemImageApi(finalItemId!, fd);
        }
      }

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

      Alert.alert("성공", "대여가 등록되었습니다.", [
        {
          text: "확인",
          onPress: () =>
            navigation.navigate("AgreementList", {
              screen: "MyAgreementsList",
            }),
        },
      ]);
    } catch (e) {
      console.error(e);
      Alert.alert("오류", "대여 등록에 실패했습니다.");
    }
  };

  const step1Done = !!rentType;
  const step2Done =
    rentType === "MONEY"
      ? !!amount.trim()
      : rentSource === "EXISTING"
      ? !!selectedItem
      : rentSource === "NEW"
      ? !!itemTitle.trim()
      : false;
  const step3Done = !!dueDate;

  const rentTypeLabel =
    rentType === "ITEM"
      ? "물건 대여"
      : rentType === "MONEY"
      ? "금전 대여"
      : "대여 타입을 선택하세요";

  return (
    <>
      {/* 날짜 선택 모달 */}
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

      {/* 기존 물건 선택 모달 */}
      <Portal>
        <Dialog
          visible={isItemModalVisible}
          onDismiss={() => setIsItemModalVisible(false)}
          style={styles.dialog}
        >
          <Dialog.Title style={styles.dialogTitle}>
            대여 가능한 물건 선택
          </Dialog.Title>
          <Dialog.Content style={[styles.dialogContent, { height: 420 }]}>
            <TextInput
              mode="outlined"
              label="물건 이름 검색"
              value={searchQuery}
              onChangeText={setSearchQuery}
              onSubmitEditing={handleSearchSubmit}
              style={styles.input}
            />
            <FlatList
              data={items}
              keyExtractor={(item) => item.id.toString()}
              refreshing={isRefreshing}
              onRefresh={onRefresh}
              onEndReached={fetchMoreItems}
              onEndReachedThreshold={0.5}
              keyboardShouldPersistTaps="handled"
              ItemSeparatorComponent={Divider}
              renderItem={({ item }) => (
                <List.Item
                  onPress={() => {
                    setItemId(item.id);
                    setSelectedItem(item);
                    setIsItemModalVisible(false);
                  }}
                  title={item.title}
                  description={item.description}
                  left={() => (
                    <Image
                      source={{
                        uri: item.fileUrl
                          ? IMG_BASE_URL + item.fileUrl
                          : "https://via.placeholder.com/150",
                      }}
                      style={{
                        width: 56,
                        height: 56,
                        borderRadius: 8,
                        marginRight: 12,
                      }}
                    />
                  )}
                  right={(p) => <List.Icon {...p} icon="chevron-right" />}
                  style={{ paddingVertical: 10 }}
                />
              )}
            />
          </Dialog.Content>
        </Dialog>
      </Portal>

      <TouchableWithoutFeedback onPress={Keyboard.dismiss} accessible={false}>
        <KeyboardAvoidingView
          style={{ flex: 1 }}
          behavior={Platform.OS === "ios" ? "padding" : "height"}
          keyboardVerticalOffset={keyboardOffset}
        >
          <SafeAreaView style={styles.safe}>
            <ScrollView
              ref={scrollRef}
              contentContainerStyle={[
                styles.container,
                { paddingBottom: insets.bottom + 24 },
              ]}
              keyboardShouldPersistTaps="handled"
              keyboardDismissMode="on-drag"
              showsVerticalScrollIndicator={false}
            >
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
                    물건이나 금전에 대한 대여 약정을 생성합니다.
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

              {debtorUser ? (
                <TouchableOpacity
                  activeOpacity={0.9}
                  onPress={() => navigation.goBack()}
                  style={styles.personCard}
                >
                  <View style={styles.personAvatar}>
                    <MaterialCommunityIcons
                      name="account"
                      size={22}
                      color="#6B7280"
                    />
                  </View>
                  <View style={{ flex: 1 }}>
                    <Text style={styles.personName}>{debtorUser.name}</Text>
                    <Text style={styles.personPhone}>{debtorUser.phone}</Text>
                  </View>
                  <View style={styles.badge}>
                    <Text style={styles.badgeText}>선택됨</Text>
                  </View>
                </TouchableOpacity>
              ) : (
                <TouchableOpacity
                  activeOpacity={0.9}
                  onPress={() => navigation.goBack()}
                  style={[styles.personCard, styles.personCardEmpty]}
                >
                  <View style={styles.personAvatar}>
                    <MaterialCommunityIcons
                      name="account"
                      size={22}
                      color="#9CA3AF"
                    />
                  </View>
                  <View style={{ flex: 1 }}>
                    <Text style={[styles.personName, { color: "#9CA3AF" }]}>
                      상대방을 선택하세요
                    </Text>
                    <Text style={[styles.personPhone, { color: "#C4C7CE" }]}>
                      연락처/검색에서 선택
                    </Text>
                  </View>
                  <MaterialCommunityIcons
                    name="chevron-right"
                    size={22}
                    color="#9CA3AF"
                  />
                </TouchableOpacity>
              )}

              <View style={[styles.sectionHeaderRow, { marginTop: 22 }]}>
                <MaterialCommunityIcons
                  name="cube-outline"
                  size={20}
                  color="#111"
                  style={{ marginRight: 8 }}
                />
                <Text style={styles.sectionHeaderTitle}>대여 타입 선택</Text>
              </View>

              <Menu
                visible={rentTypeMenuOpen}
                onDismiss={() => setRentTypeMenuOpen(false)}
                anchor={
                  <TouchableOpacity
                    activeOpacity={0.9}
                    onPress={() => setRentTypeMenuOpen(true)}
                    style={styles.selectBox}
                  >
                    <Text
                      style={[
                        styles.selectBoxLabel,
                        !rentType && { color: "#9CA3AF" },
                      ]}
                      numberOfLines={1}
                    >
                      {rentTypeLabel}
                    </Text>
                    <MaterialCommunityIcons
                      name="chevron-down"
                      size={20}
                      color="#9CA3AF"
                    />
                  </TouchableOpacity>
                }
                contentStyle={styles.menuSurface}
              >
                <List.Item
                  onPress={() => {
                    handleRentTypeChange("ITEM");
                    setRentTypeMenuOpen(false);
                  }}
                  title="물건 대여"
                  description="물리적인 물건을 대여합니다"
                  left={(p) => <List.Icon {...p} icon="cube-outline" />}
                />
                <Divider />
                <List.Item
                  onPress={() => {
                    handleRentTypeChange("MONEY");
                    setRentTypeMenuOpen(false);
                  }}
                  title="금전 대여"
                  description="현금을 대여합니다"
                  left={(p) => <List.Icon {...p} icon="currency-usd" />}
                />
              </Menu>

              {step1Done && rentType === "ITEM" && (
                <>
                  <View style={[styles.sectionHeaderRow, { marginTop: 24 }]}>
                    <MaterialCommunityIcons
                      name="cube-outline"
                      size={20}
                      color="#111"
                      style={{ marginRight: 8 }}
                    />
                    <Text style={styles.sectionHeaderTitle}>물건 설정</Text>
                  </View>

                  <Menu
                    visible={rentSourceMenuOpen}
                    onDismiss={() => setRentSourceMenuOpen(false)}
                    anchor={
                      <TouchableOpacity
                        activeOpacity={0.9}
                        onPress={() => setRentSourceMenuOpen(true)}
                        style={styles.selectBox}
                      >
                        <Text
                          style={[
                            styles.selectBoxLabel,
                            !rentSource && { color: "#9CA3AF" },
                          ]}
                          numberOfLines={1}
                        >
                          {rentSource === "EXISTING"
                            ? "기존 물건 선택"
                            : rentSource === "NEW"
                            ? "새로 등록"
                            : "선택 방식을 고르세요"}
                        </Text>
                        <MaterialCommunityIcons
                          name="chevron-down"
                          size={20}
                          color="#9CA3AF"
                        />
                      </TouchableOpacity>
                    }
                    contentStyle={styles.menuSurface}
                  >
                    <List.Item
                      left={(p) => <List.Icon {...p} icon="tray-arrow-up" />}
                      title="기존 물건 선택"
                      onPress={() => {
                        handleRentSourceChange("EXISTING");
                        setRentSourceMenuOpen(false);
                      }}
                    />
                    <Divider />
                    <List.Item
                      left={(p) => (
                        <List.Icon {...p} icon="plus-box-multiple-outline" />
                      )}
                      title="새로 등록"
                      onPress={() => {
                        handleRentSourceChange("NEW");
                        setRentSourceMenuOpen(false);
                      }}
                    />
                  </Menu>

                  {rentSource === "EXISTING" && (
                    <View style={{ marginTop: 12 }}>
                      <TouchableOpacity
                        style={styles.outlinedButton}
                        activeOpacity={0.9}
                        onPress={() => {
                          handleFilterPress(AVAILABLE_STATUS);
                          setIsItemModalVisible(true);
                        }}
                      >
                        <MaterialCommunityIcons
                          name="tray-arrow-up"
                          size={18}
                          color="#5B6166"
                          style={{ marginRight: 6 }}
                        />
                        <Text style={styles.outlinedButtonText}>
                          기존 물건 선택하기
                        </Text>
                      </TouchableOpacity>

                      {selectedItem && (
                        <View style={{ marginTop: 12 }}>
                          <View style={styles.selectedItemCard}>
                            <Image
                              source={{
                                uri: selectedItem.fileUrl
                                  ? IMG_BASE_URL + selectedItem.fileUrl
                                  : "https://via.placeholder.com/300",
                              }}
                              style={styles.selectedItemImage}
                              resizeMode="cover"
                            />
                            <Text
                              style={styles.selectedItemTitle}
                              numberOfLines={2}
                            >
                              {selectedItem.title}
                            </Text>
                            {!!selectedItem.description && (
                              <Text
                                style={styles.selectedItemDesc}
                                numberOfLines={4}
                              >
                                {selectedItem.description}
                              </Text>
                            )}
                          </View>
                        </View>
                      )}
                    </View>
                  )}

                  {rentSource === "NEW" && (
                    <View style={styles.card}>
                      <Text style={styles.fieldLabel}>물건 제목</Text>
                      <TextInput
                        mode="outlined"
                        placeholder="물건 제목을 입력하세요"
                        value={itemTitle}
                        onChangeText={setItemTitle}
                        style={styles.input}
                      />

                      <Text style={styles.fieldLabel}>물건 사진</Text>
                      <TouchableOpacity
                        style={styles.outlinedButton}
                        activeOpacity={0.9}
                        onPress={pickImage}
                      >
                        <MaterialCommunityIcons
                          name="cloud-upload-outline"
                          size={18}
                          color="#5B6166"
                          style={{ marginRight: 6 }}
                        />
                        <Text style={styles.outlinedButtonText}>
                          이미지 업로드
                        </Text>
                      </TouchableOpacity>
                      {itemImage && (
                        <Image
                          source={{ uri: itemImage.uri }}
                          style={styles.preview}
                          resizeMode="cover"
                        />
                      )}

                      <Text style={[styles.fieldLabel, { marginTop: 12 }]}>
                        물건 설명
                      </Text>
                      <TextInput
                        mode="outlined"
                        placeholder="물건에 대한 상세 설명을 입력하세요"
                        value={itemDesc}
                        onChangeText={setItemDesc}
                        multiline
                        textAlignVertical="top"
                        style={[styles.input, { minHeight: 120 }]}
                        returnKeyType="done"
                        blurOnSubmit
                        onFocus={() =>
                          setTimeout(
                            () =>
                              scrollRef.current?.scrollToEnd({
                                animated: true,
                              }),
                            150
                          )
                        }
                      />
                    </View>
                  )}
                </>
              )}

              {step1Done && rentType === "MONEY" && (
                <>
                  <View style={[styles.sectionHeaderRow, { marginTop: 24 }]}>
                    <MaterialCommunityIcons
                      name="currency-usd"
                      size={20}
                      color="#111"
                      style={{ marginRight: 8 }}
                    />
                    <Text style={styles.sectionHeaderTitle}>
                      금전 대여 설정
                    </Text>
                  </View>
                  <View style={styles.card}>
                    <Text style={styles.fieldLabel}>대여 금액</Text>
                    <TextInput
                      mode="outlined"
                      placeholder="대여할 금액을 입력하세요"
                      value={amount}
                      onChangeText={setAmount}
                      keyboardType="numeric"
                      style={styles.input}
                    />
                  </View>
                </>
              )}

              {step2Done && (
                <>
                  <View style={[styles.sectionHeaderRow, { marginTop: 24 }]}>
                    <MaterialCommunityIcons
                      name="calendar"
                      size={20}
                      color="#111"
                      style={{ marginRight: 8 }}
                    />
                    <Text style={styles.sectionHeaderTitle}>
                      반납일 및 상세 조건
                    </Text>
                  </View>
                  <View style={styles.card}>
                    <Text style={styles.fieldLabel}>반납일</Text>
                    <TouchableOpacity
                      style={styles.outlinedButton}
                      activeOpacity={0.9}
                      onPress={() => setIsDateModalVisible(true)}
                    >
                      <MaterialCommunityIcons
                        name="calendar"
                        size={18}
                        color="#5B6166"
                        style={{ marginRight: 6 }}
                      />
                      <Text style={styles.outlinedButtonText}>
                        {dueDate
                          ? `반납일: ${dueDate.toLocaleDateString()}`
                          : "반납일을 선택하세요"}
                      </Text>
                    </TouchableOpacity>

                    <Text style={[styles.fieldLabel, { marginTop: 12 }]}>
                      대여 상세 조건
                    </Text>
                    <TextInput
                      mode="outlined"
                      placeholder="대여 조건, 주의사항 등을 입력하세요"
                      value={terms}
                      onChangeText={setTerms}
                      multiline
                      textAlignVertical="top"
                      style={[styles.input, { minHeight: 140 }]}
                      onFocus={() =>
                        setTimeout(
                          () =>
                            scrollRef.current?.scrollToEnd({ animated: true }),
                          150
                        )
                      }
                    />
                  </View>
                </>
              )}

              {step3Done && (
                <View
                  style={{
                    paddingHorizontal: 12,
                    marginTop: 16,
                    marginBottom: 28,
                  }}
                >
                  <TouchableOpacity
                    style={styles.outlinedButton}
                    activeOpacity={0.9}
                    onPress={handleCreate}
                  >
                    <Text style={styles.outlinedButtonText}>대여 요청하기</Text>
                  </TouchableOpacity>
                </View>
              )}
            </ScrollView>
          </SafeAreaView>
        </KeyboardAvoidingView>
      </TouchableWithoutFeedback>
    </>
  );
}

const styles = StyleSheet.create({
  safe: {
    flex: 1,
    backgroundColor: "#ffffff",
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
  pageHeaderTitle: { fontSize: 25, fontWeight: "800", color: "#111" },
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
    marginBottom: 20,
    paddingLeft: 2,
  },
  sectionHeaderTitle: {
    fontSize: 20,
    fontWeight: "800",
    color: "#111",
  },
  selectBox: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    borderWidth: 1,
    borderColor: "#E6E6E6",
    borderRadius: 12,
    paddingHorizontal: 14,
    paddingVertical: 14,
    backgroundColor: "#F7F7F7",
  },
  selectBoxLabel: {
    fontSize: 15,
    color: "#2F3438",
    fontWeight: "600",
  },
  menuSurface: {
    backgroundColor: "#FFFFFF",
    borderRadius: 14,
    paddingVertical: 4,
  },
  fieldLabel: {
    fontSize: 14,
    color: "#4B5563",
    marginLeft: 2,
    marginBottom: 6,
  },
  card: {
    backgroundColor: "#F7F8FA",
    borderRadius: 16,
    padding: 14,
    marginTop: 8,
    borderWidth: 1,
    borderColor: "#EEF0F2",
  },
  outlinedButton: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    borderWidth: 1,
    borderColor: "#E5E7EB",
    backgroundColor: "#FFFFFF",
    borderRadius: 12,
    paddingVertical: 14,
  },
  outlinedButtonText: {
    color: "#2F3438",
    fontWeight: "700",
  },
  personCard: {
    flexDirection: "row",
    alignItems: "center",
    padding: 14,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: "#E5E7EB",
    backgroundColor: "#F7F7F7",
    marginBottom: 6,
  },
  personCardEmpty: {
    backgroundColor: "#FAFAFA",
  },
  personAvatar: {
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: "#E9ECEF",
    alignItems: "center",
    justifyContent: "center",
    marginRight: 12,
  },
  personName: {
    fontSize: 16,
    fontWeight: "700",
    color: "#111",
  },
  personPhone: {
    fontSize: 13,
    color: "#8B95A1",
    marginTop: 4,
  },
  badge: {
    paddingVertical: 4,
    paddingHorizontal: 8,
    borderRadius: 10,
    backgroundColor: "#E6F8EE",
  },
  badgeText: {
    color: "#199A5E",
    fontWeight: "700",
    fontSize: 12,
  },
  thumb: {
    width: 56,
    height: 56,
    borderRadius: 8,
    marginRight: 12,
  },
  preview: {
    width: "100%",
    height: 200,
    borderRadius: 10,
    marginTop: 8,
  },
  avatar: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: "#E7E7E7",
    alignItems: "center",
    justifyContent: "center",
    marginRight: 12,
  },
  avatarText: {
    fontWeight: "700",
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
  input: {
    marginBottom: 12,
    backgroundColor: "transparent",
  },
  selectedItemCard: {
    backgroundColor: "#FFFFFF",
    borderWidth: 1,
    borderColor: "#E5E7EB",
    borderRadius: 14,
    padding: 12,
    alignItems: "center",
  },
  selectedItemImage: {
    width: "100%",
    height: 180,
    borderRadius: 10,
    marginBottom: 10,
  },
  selectedItemTitle: {
    fontSize: 16,
    fontWeight: "800",
    color: "#111",
    marginBottom: 6,
  },
  selectedItemDesc: {
    fontSize: 13,
    color: "#6B7280",
    lineHeight: 19,
  },
});
