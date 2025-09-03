import React, { useState } from "react";
import {
  Alert,
  Image,
  Keyboard,
  StyleSheet,
  TouchableOpacity,
  TouchableWithoutFeedback,
  View,
} from "react-native";
import { Appbar, Button, Text, TextInput } from "react-native-paper";
import * as ImagePicker from "expo-image-picker";
import { updateItemApi, updateItemImageApi } from "../../../api/items";
import * as ImageManipulator from "expo-image-manipulator";
import { IMG_BASE_URL } from "@env";

const MAX_DIM = 1280;

const MyItemEditScreen = ({ route, navigation }: any) => {
  const { item } = route.params;

  const [title, setTitle] = useState(item.title);
  const [description, setDescription] = useState(item.description);
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
      await updateItemApi(item.id, { title, description });

      if (image) {
        const formData = new FormData();
        formData.append("img", {
          uri: image.uri,
          name: image.name,
          type: image.type,
        } as any);
        await updateItemImageApi(item.id, formData);
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

  return (
    <TouchableWithoutFeedback onPress={Keyboard.dismiss}>
      <View style={styles.container}>
        <Appbar.Header>
          <Appbar.BackAction onPress={() => navigation.goBack()} />
          <Appbar.Content title="대여물품 수정" />
        </Appbar.Header>

        <View style={styles.content}>
          <TouchableOpacity onPress={pickImage} style={styles.imageContainer}>
            <Image
              source={{
                uri: image?.uri
                  ? image.uri
                  : item?.fileUrl
                  ? IMG_BASE_URL + item.fileUrl
                  : "https://via.placeholder.com/400x300",
              }}
              style={styles.image}
            />
          </TouchableOpacity>

          <TextInput
            label="물품 이름"
            value={title}
            onChangeText={setTitle}
            style={styles.input}
          />
          <TextInput
            label="물품 설명"
            value={description}
            onChangeText={setDescription}
            multiline
            numberOfLines={4}
            style={styles.input}
          />
          <Button
            mode="contained"
            onPress={handleUpdate}
            loading={isLoading}
            disabled={isLoading}
            style={styles.button}
          >
            수정 완료
          </Button>
        </View>
      </View>
    </TouchableWithoutFeedback>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#fff",
  },
  content: {
    padding: 16,
  },
  imageContainer: {
    alignItems: "center",
    marginBottom: 20,
  },
  image: {
    width: "100%",
    height: 250,
    borderRadius: 8,
    backgroundColor: "#eee",
  },
  imageText: {
    marginTop: 8,
    color: "blue",
  },
  input: {
    marginBottom:20,
    height: 60,
    backgroundColor: "transparent",
  },
  button: {
    marginTop: 8,
  },
});

export default MyItemEditScreen;
