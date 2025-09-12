import React from "react";
import {
  Image,
  Keyboard,
  StyleSheet,
  TouchableOpacity,
  TouchableWithoutFeedback,
  View
} from "react-native";
import { Appbar, Button, TextInput } from "react-native-paper";

import { useItemEdit } from "./hooks/useItemEdit";

const MyItemEditScreen = ({ route, navigation }: any) => {
  const { item } = route.params;
  const {
    title,
    setTitle,
    description,
    setDescription,
    image,
    isLoading,
    pickImage,
    handleUpdate,
  } = useItemEdit(item, navigation);

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
                  ? item.fileUrl
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
    marginBottom: 20,
    height: 60,
    backgroundColor: "transparent",
  },
  button: {
    marginTop: 8,
  },
});

export default MyItemEditScreen;
