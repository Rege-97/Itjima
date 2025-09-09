import { useCallback, useRef, useState } from "react";
import { Alert } from "react-native";
import * as ImageManipulator from "expo-image-manipulator";
import * as ImagePicker from "expo-image-picker";
import { useFocusEffect } from "@react-navigation/native";
import { useItem } from "../../../items/MyItemScreen/hooks/useItem";
import { createItemApi, updateItemImageApi } from "../../../../api/items";
import { agreementCreateApi } from "../../../../api/agreements";

export const MAX_DIM = 1280;
export const AVAILABLE_STATUS = "AVAILABLE" as const;

export type User = { id: number; name: string; phone: string };
export type RentType = "ITEM" | "MONEY";
export type RentSource = "EXISTING" | "NEW";

export function useMyAgreementCreate(params: {
  initialDebtor: User | null;
  navigation: any;
}) {
  const { initialDebtor, navigation } = params;

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

  const scrollRef = useRef<any>(null);

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

  return {
    items,
    isRefreshing,
    fetchMoreItems,
    onRefresh,
    searchQuery,
    setSearchQuery,
    handleSearchSubmit,
    handleFilterPress,
    isItemModalVisible,
    setIsItemModalVisible,
    rentTypeMenuOpen,
    setRentTypeMenuOpen,
    rentSourceMenuOpen,
    setRentSourceMenuOpen,
    debtorUser,
    setDebtorUser,
    rentType,
    setRentType,
    rentSource,
    setRentSource,
    amount,
    setAmount,
    itemTitle,
    setItemTitle,
    itemDesc,
    setItemDesc,
    itemImage,
    setItemImage,
    itemId,
    setItemId,
    selectedItem,
    setSelectedItem,
    dueDate,
    setDueDate,
    isDateModalVisible,
    setIsDateModalVisible,
    terms,
    setTerms,
    pickImage,
    handleRentTypeChange,
    handleRentSourceChange,
    handleCreate,
    step1Done,
    step2Done,
    step3Done,
    rentTypeLabel,
    scrollRef,
  };
}
