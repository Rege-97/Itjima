import React from "react";
import { StyleSheet, View } from "react-native";
import { Button, Text } from "react-native-paper";

const RegisterWelcomeScreen = ({ navigation }: any) => {
  return (
    <View style={styles.container}>
      <Text variant="headlineLarge" style={styles.title}>
        잊지마에 오신 것을 환영합니다!
      </Text>
      <Text variant="bodyLarge" style={styles.subtitle}>
        간단한 정보만으로 시작할 수 있어요.
      </Text>
      <Button
        mode="contained"
        style={styles.button}
        onPress={() => navigation.navigate("RegisterForm")}
      >
        가입 시작하기
      </Button>
    </View>
  );
};
const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    justifyContent: "center",
    alignItems: "center",
  },
  title: {
    textAlign: "center",
    marginBottom: 16,
    fontSize: 28,
  },
  subtitle: {
    textAlign: "center",
    marginBottom: 32,
  },
  button: {
    width: "100%",
  },
});
export default RegisterWelcomeScreen;
