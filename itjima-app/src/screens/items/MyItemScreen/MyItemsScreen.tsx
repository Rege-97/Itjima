import React, { useState } from "react";
import {
  FlatList,
  RefreshControl,
  StyleSheet,
  TouchableOpacity,
  View,
} from "react-native";
import { ActivityIndicator, FAB, Searchbar, Text } from "react-native-paper";
import ItemCard from "./components/ItemCard";
import SummaryBox from "./components/SummaryBox";
import { useItem } from "./hooks/useItem";
import { MaterialIcons } from "@expo/vector-icons";
import { SafeAreaView } from "react-native-safe-area-context";

const MyItemsScreen = ({ navigation }: any) => {
  const {
    items,
    isLoading,
    isRefreshing,
    isLoadingMore,
    hasNext,
    counts,
    searchQuery,
    activeFilter,
    fetchInitialItems,
    setSearchQuery,
    handleSearchChange,
    handleSearchSubmit,
    handleFilterPress,
    fetchMoreItems,
    onRefresh,
  } = useItem(navigation);

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
          내 물품
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
      <FlatList
        ListHeaderComponent={
          <>
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
            <SummaryBox
              counts={counts}
              activeFilter={activeFilter}
              onFilterPress={handleFilterPress}
            />
          </>
        }
        data={items}
        keyExtractor={(item) => item.id.toString()}
        renderItem={({ item }) => (
          <ItemCard item={item} navigation={navigation} />
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

      <FAB
        icon="plus"
        color="#fff"
        style={styles.fab}
        onPress={() => navigation.navigate("MyItemCreate")}
        label="물품 추가"
      />
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#fff",
  },

  fab: {
    position: "absolute",
    right: 16,
    bottom: 16,
    backgroundColor: "#6200ee",
  },
  searchbar: {
    backgroundColor: "#ffffffff",
    borderRadius: 0,
  },
});

export default MyItemsScreen;
