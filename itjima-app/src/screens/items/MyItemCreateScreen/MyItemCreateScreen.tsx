import React from "react";
import {
  Image,
  Keyboard,
  StyleSheet,
  TouchableOpacity,
  TouchableWithoutFeedback,
  View,
} from "react-native";

import { Appbar, Button, TextInput } from "react-native-paper";
import { useItemCreate } from "./hooks/useItemCreate";
import { MaterialCommunityIcons } from "@expo/vector-icons";

const MyItemCreateScreen = ({ navigation }: any) => {
  const {
    title,
    setTitle,
    description,
    setDescription,
    image,
    isLoading,
    pickImage,
    handleCreate,
  } = useItemCreate(navigation);

  return (
    <TouchableWithoutFeedback onPress={Keyboard.dismiss}>
      <View style={styles.container}>
        <Appbar.Header>
          <Appbar.BackAction onPress={() => navigation.goBack()} />
          <Appbar.Content title="대여물품 등록" />
        </Appbar.Header>

        <View style={styles.content}>
          <TouchableOpacity onPress={pickImage} style={styles.imageContainer}>
            {image?.uri ? (
              <Image source={{ uri: image.uri }} style={styles.image} />
            ) : (
              <View style={styles.placeholder}>
                <MaterialCommunityIcons
                  name="plus"
                  size={48}
                  color="#9CA3AF" // 회색 느낌
                />
              </View>
            )}
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
            onPress={handleCreate}
            loading={isLoading}
            disabled={isLoading}
            style={styles.button}
          >
            등록
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
  placeholder: {
    width: "100%",
    height: 250,
    borderRadius: 8,
    backgroundColor: "#E5E7EB",
    justifyContent: "center",
    alignItems: "center",
  },
});

export default MyItemCreateScreen;
