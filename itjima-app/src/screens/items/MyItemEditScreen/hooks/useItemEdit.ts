import * as ImageManipulator from "expo-image-manipulator";
import * as ImagePicker from "expo-image-picker";
import { useState } from "react";
import { Alert } from "react-native";
import { updateItemApi, updateItemImageApi } from "../../../../api/items";

const MAX_DIM = 1280;

export const useItemEdit = (initialItem: any, navigation: any) => {
  const [title, setTitle] = useState<string>(initialItem.title);
  const [description, setDescription] = useState<string>(
    initialItem.description
  );
  const [image, setImage] = useState<{
    uri: string;
    name: string;
    type: string;
  } | null>(null);
  const [isLoading, setIsLoading] = useState(false);

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

    setImage({
      uri: manipulated.uri,
      name: finalName,
      type: "image/jpeg",
    });
  };

  const handleUpdate = async () => {
    if (!title) {
      Alert.alert("오류", "물품 이름을 입력해주세요.");
      return;
    }
    setIsLoading(true);
    try {
      await updateItemApi(initialItem.id, { title, description });

      if (image) {
        const formData = new FormData();
        formData.append("img", {
          uri: image.uri,
          name: image.name,
          type: image.type,
        } as any);
        await updateItemImageApi(initialItem.id, formData);
      }
      Alert.alert("성공", "물품 정보가 성공적으로 수정되었습니다.");
      navigation.goBack();
    } catch (error) {
      console.error("물품 수정 실패:", error);
      Alert.alert("오류", "물품 정보 수정에 실패했습니다.");
    } finally {
      setIsLoading(false);
    }
  };

  return {
    title,
    setTitle,
    description,
    setDescription,
    image,
    isLoading,
    pickImage,
    handleUpdate,
  };
};
