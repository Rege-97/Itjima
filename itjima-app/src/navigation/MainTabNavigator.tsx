import { MaterialCommunityIcons } from "@expo/vector-icons";
import { createBottomTabNavigator } from "@react-navigation/bottom-tabs";
import { useIsFocused } from "@react-navigation/native";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import React from "react";
import MyAgreementCreateScreen from "../screens/agreements/MyAgreementCreateScreen/MyAgreementCreateScreen";
import PartnerSelectScreen from "../screens/agreements/MyAgreementCreateScreen/PartnerSelectScreen";
import MyAgreementDetailScreen from "../screens/agreements/MyAgreementDetailScreen/MyAgreementDetailScreen";
import MyAgreementsScreen from "../screens/agreements/MyAgreementsScreen/MyAgreementsScreen";
import HomeScreen from "../screens/home/HomeScreen";
import MyItemCreateScreen from "../screens/items/MyItemCreateScreen/MyItemCreateScreen";
import MyItemDetailScreen from "../screens/items/MyItemDetailScreen/MyItemDetailScreen";
import MyItemEditScreen from "../screens/items/MyItemEditScreen/MyItemEditScreen";
import MyItemsScreen from "../screens/items/MyItemScreen/MyItemsScreen";
import ProfileScreen from "../screens/profile/ProfileScreen";

const FocusUnmount: React.FC<React.PropsWithChildren> = ({ children }) => {
  const isFocused = useIsFocused();
  return isFocused ? <>{children}</> : null;
};

const MyItemsStack = createNativeStackNavigator();

const MyItemsStackNavigator = () => {
  return (
    <FocusUnmount>
      <MyItemsStack.Navigator
        initialRouteName="MyItemsList"
        screenOptions={{ headerShown: false }}
      >
        <MyItemsStack.Screen name="MyItemsList" component={MyItemsScreen} />
        <MyItemsStack.Screen
          name="MyItemDetail"
          component={MyItemDetailScreen}
        />
        <MyItemsStack.Screen name="MyItemEdit" component={MyItemEditScreen} />
        <MyItemsStack.Screen
          name="MyItemCreate"
          component={MyItemCreateScreen}
        />
      </MyItemsStack.Navigator>
    </FocusUnmount>
  );
};

const MyAgreementsStack = createNativeStackNavigator();

const MyAgreementsStackNavigator = () => {
  return (
    <FocusUnmount>
      <MyAgreementsStack.Navigator
        initialRouteName="MyAgreementsList"
        screenOptions={{ headerShown: false }}
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
    </FocusUnmount>
  );
};

const MyAgreementCreateStack = createNativeStackNavigator();

const MyAgreementCreateStackNavigator = () => {
  return (
    <FocusUnmount>
      <MyAgreementCreateStack.Navigator
        initialRouteName="PartnerSelect"
        screenOptions={{ headerShown: false }}
      >
        <MyAgreementCreateStack.Screen
          name="PartnerSelect"
          component={PartnerSelectScreen}
        />
        <MyAgreementCreateStack.Screen
          name="MyAgreementCreate"
          component={MyAgreementCreateScreen}
        />
      </MyAgreementCreateStack.Navigator>
    </FocusUnmount>
  );
};

const HomeStack = createNativeStackNavigator();

const HomeStackNavigator = () => {
  return (
    <FocusUnmount>
      <HomeStack.Navigator
        initialRouteName="HomeScreen"
        screenOptions={{ headerShown: false }}
      >
        <HomeStack.Screen name="HomeScreen" component={HomeScreen} />
      </HomeStack.Navigator>
    </FocusUnmount>
  );
};

const ProfileStack = createNativeStackNavigator();

const ProfileStackNavigator = () => {
  return (
    <FocusUnmount>
      <ProfileStack.Navigator
        initialRouteName="ProfileScreen"
        screenOptions={{ headerShown: false }}
      >
        <ProfileStack.Screen name="ProfileScreen" component={ProfileScreen} />
      </ProfileStack.Navigator>
    </FocusUnmount>
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
        component={HomeStackNavigator}
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
        component={MyAgreementCreateStackNavigator}
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
          title: "내 물품",
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
        component={ProfileStackNavigator}
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
    </Tab.Navigator>
  );
};

export default MainTabNavigator;
