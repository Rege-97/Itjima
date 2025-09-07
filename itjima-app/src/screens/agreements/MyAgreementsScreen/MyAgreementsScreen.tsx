import React, { useCallback, useState } from "react";
import {
  FlatList,
  RefreshControl,
  SafeAreaView,
  StyleSheet,
  TouchableOpacity,
  View,
} from "react-native";
import {
  ActivityIndicator,
  Searchbar,
  SegmentedButtons,
  Text,
} from "react-native-paper";
import { getMyAgreementsApi } from "../../../api/agreements";
import { useFocusEffect } from "@react-navigation/native";
import { MaterialIcons } from "@expo/vector-icons";
import AgreementCard from "./components/AgreementCard";
import { useAgreement } from "./hooks/useAgreement";

const MyAgreementsScreen = ({ navigation }: any) => {
  const {
    agreements,
    isLoading,
    isRefreshing,
    isLoadingMore,
    hasNext,
    searchQuery,
    activeFilter,
    fetchInitialItems,
    setSearchQuery,
    handleSearchChange,
    handleSearchSubmit,
    handleFilterPress,
    fetchMoreItems,
    onRefresh,
  } = useAgreement(navigation);

  const [viewSearch, setViewSearch] = useState(false);

  if (isLoading) {
    return (
      <View style={{ flex: 1, justifyContent: "center", alignItems: "center" }}>
        <ActivityIndicator animating={true} size="large" />
      </View>
    );
  }

  return (
    <SafeAreaView style={styles.container}>
      <View style={{ justifyContent: "space-between", flexDirection: "row" }}>
        <Text style={{ fontSize: 24, fontWeight: "bold", margin: 16 }}>
          대여 목록
        </Text>
        <TouchableOpacity
          style={{ justifyContent: "center", marginRight: 10 }}
          onPress={() => {
            if (viewSearch) {
              setViewSearch(false);
            } else {
              setViewSearch(true);
            }
          }}
        >
          <MaterialIcons name="search" size={30} />
        </TouchableOpacity>
      </View>
      {viewSearch && (
        <Searchbar
          placeholder="물품명으로 검색"
          value={searchQuery}
          onChangeText={handleSearchChange}
          onSubmitEditing={handleSearchSubmit}
          onClearIconPress={() => {
            setSearchQuery("");
            fetchInitialItems(activeFilter, "");
          }}
          style={styles.searchbar}
        />
      )}
      <SegmentedButtons
        value={activeFilter ?? "ALL"}
        onValueChange={handleFilterPress}
        style={styles.segmentedContainer}
        buttons={[
          {
            value: "ALL",
            label: "전체",
            style: styles.segmentedButton,
            labelStyle: styles.segmentedLabel,
          },
          {
            value: "CREDITOR",
            label: "빌려준 것",
            style: styles.segmentedButton,
            labelStyle: styles.segmentedLabel,
          },
          {
            value: "DEBTOR",
            label: "빌린 것",
            style: styles.segmentedButton,
            labelStyle: styles.segmentedLabel,
          },
        ]}
        theme={{
          colors: {
            primary: "#fff",
            onPrimary: "#000",
            surface: "#f0f0f3",
            outline: "transparent",
          },
        }}
      />
      <FlatList
        ListHeaderComponent={<></>}
        data={agreements}
        keyExtractor={(agreement) => agreement.id.toString()}
        renderItem={({ item }) => (
          <AgreementCard agreement={item} navigation={navigation} />
        )}
        contentContainerStyle={{ padding: 8 }}
        refreshControl={
          <RefreshControl refreshing={isRefreshing} onRefresh={onRefresh} />
        }
        onEndReached={fetchMoreItems}
        onEndReachedThreshold={0.2}
        ListFooterComponent={
          hasNext && isLoadingMore ? (
            <View style={{ padding: 20 }}>
              <ActivityIndicator animating={true} />
            </View>
          ) : null
        }
      />
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#fff",
  },
  searchbar: {
    backgroundColor: "#ffffffff",
    borderRadius: 0,
  },
  segmentedContainer: {
    marginHorizontal: 14,
    marginVertical: 8,
    borderRadius: 6,
    height: 40,
    backgroundColor: "#f0f0f3",
  },
  segmentedButton: {
    flex: 1,
    borderRadius: 6,
  },
  segmentedLabel: {
    fontSize: 13,
    fontWeight: "600",
  },
});

export default MyAgreementsScreen;
