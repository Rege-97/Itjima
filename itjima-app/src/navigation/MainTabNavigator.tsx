import { MaterialCommunityIcons } from "@expo/vector-icons";
import { createBottomTabNavigator } from "@react-navigation/bottom-tabs";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import React from "react";
import { View } from "react-native";
import { Button, Text } from "react-native-paper";
import { useAuth } from "../contexts/AuthContext";
import MyItemCreateScreen from "../screens/items/MyItemCreateScreen/MyItemCreateScreen";
import MyItemDetailScreen from "../screens/items/MyItemDetailScreen/MyItemDetailScreen";
import MyItemEditScreen from "../screens/items/MyItemEditScreen/MyItemEditScreen";
import MyItemsScreen from "../screens/items/MyItemScreen/MyItemsScreen";
import MyAgreementsScreen from "../screens/agreements/MyAgreementsScreen/MyAgreementsScreen";
import MyAgreementDetailScreen from "../screens/agreements/MyAgreementDetailScreen/MyAgreementDetailScreen";

const HomeScreen = () => (
  <View>
    <Text>홈</Text>
  </View>
);
const AddAgreementScreen = () => (
  <View>
    <Text>등록</Text>
  </View>
);
const ProfileScreen = () => (
  <View>
    <Text>프로필</Text>
  </View>
);

// ✅ 임시 로그아웃 스크린
const LogoutScreen = () => {
  const { logout } = useAuth();
  return (
    <View style={{ flex: 1, justifyContent: "center", alignItems: "center" }}>
      <Text variant="titleMedium">로그아웃 테스트</Text>
      <Button mode="contained" style={{ marginTop: 16 }} onPress={logout}>
        로그아웃
      </Button>
    </View>
  );
};

const MyItemsStack = createNativeStackNavigator();

const MyItemsStackNavigator = () => {
  return (
    <MyItemsStack.Navigator
      initialRouteName="MyItemsList"
      screenOptions={{ headerShown: false }}
    >
      <MyItemsStack.Screen name="MyItemsList" component={MyItemsScreen} />
      <MyItemsStack.Screen name="MyItemDetail" component={MyItemDetailScreen} />
      <MyItemsStack.Screen name="MyItemEdit" component={MyItemEditScreen} />
      <MyItemsStack.Screen name="MyItemCreate" component={MyItemCreateScreen} />
    </MyItemsStack.Navigator>
  );
};

const MyAgreementsStack = createNativeStackNavigator();

const MyAgreementsStackNavigator = () => {
  return (
    <MyAgreementsStack.Navigator
      initialRouteName="MyAgreementsList"
      screenOptions={{headerShown:false}}
    >
      <MyAgreementsStack.Screen
        name="MyAgreementsList"
        component={MyAgreementsScreen}
      />
      <MyAgreementsStack.Screen
      name="MyAgreementDetail"
      component={MyAgreementDetailScreen}
      />
    </MyAgreementsStack.Navigator>
  );
};

const Tab = createBottomTabNavigator();

const MainTabNavigator = () => {
  return (
    <Tab.Navigator
      initialRouteName="Home"
      screenOptions={{ tabBarActiveTintColor: "#6200ee", headerShown: false }}
    >
      <Tab.Screen
        name="Home"
        component={HomeScreen}
        options={{
          title: "홈",
          tabBarIcon: ({ color, size }) => (
            <MaterialCommunityIcons
              name="home-variant"
              color={color}
              size={size}
            />
          ),
        }}
      />
      <Tab.Screen
        name="AgreementList"
        component={MyAgreementsStackNavigator}
        options={{
          title: "목록",
          tabBarIcon: ({ color, size }) => (
            <MaterialCommunityIcons
              name="format-list-bulleted"
              color={color}
              size={size}
            />
          ),
        }}
      />
      <Tab.Screen
        name="AddAgreement"
        component={AddAgreementScreen}
        options={{
          title: "등록",
          tabBarIcon: ({ color, size }) => (
            <MaterialCommunityIcons
              name="plus-circle"
              color={color}
              size={size}
            />
          ),
        }}
      />
      <Tab.Screen
        name="MyItems"
        component={MyItemsStackNavigator}
        options={{
          title: "물품",
          tabBarIcon: ({ color, size }) => (
            <MaterialCommunityIcons
              name="briefcase-variant"
              color={color}
              size={size}
            />
          ),
        }}
      />
      <Tab.Screen
        name="Profile"
        component={ProfileScreen}
        options={{
          title: "프로필",
          tabBarIcon: ({ color, size }) => (
            <MaterialCommunityIcons
              name="account-circle"
              color={color}
              size={size}
            />
          ),
        }}
      />
      <Tab.Screen
        name="Logout"
        component={LogoutScreen}
        options={{
          title: "로그아웃",
          tabBarIcon: ({ color, size }) => (
            <MaterialCommunityIcons name="logout" color={color} size={size} />
          ),
        }}
      />
    </Tab.Navigator>
  );
};

export default MainTabNavigator;
