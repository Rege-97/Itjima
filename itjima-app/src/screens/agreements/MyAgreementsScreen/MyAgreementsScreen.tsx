import { MaterialIcons } from "@expo/vector-icons";
import { MaterialCommunityIcons } from "@expo/vector-icons";
import React, { useState } from "react";
import {
  FlatList,
  RefreshControl,
  StyleSheet,
  TouchableOpacity,
  View,
} from "react-native";
import {
  ActivityIndicator,
  Searchbar,
  Text,
} from "react-native-paper";
import AgreementCard from "./components/AgreementCard";
import { useAgreement } from "./hooks/useAgreement";
import { SafeAreaView } from "react-native-safe-area-context";

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
      {/* 헤더 */}
      <View style={{ justifyContent: "space-between", flexDirection: "row" }}>
        <Text style={{ fontSize: 24, fontWeight: "bold", margin: 16 }}>
          대여 목록
        </Text>
        <TouchableOpacity
          style={{ justifyContent: "center", marginRight: 10 }}
          onPress={() => setViewSearch((v) => !v)}
        >
          <MaterialIcons name="search" size={30} />
        </TouchableOpacity>
      </View>

      {/* 검색바 */}
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

      <FilterPills
        value={(activeFilter as "ALL" | "CREDITOR" | "DEBTOR") ?? "ALL"}
        onChange={handleFilterPress}
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
function FilterPills({
  value,
  onChange,
}: {
  value: "ALL" | "CREDITOR" | "DEBTOR";
  onChange: (v: "ALL" | "CREDITOR" | "DEBTOR") => void;
}) {
  return (
    <View style={styles.pillsRow}>
      <Pill
        active={value === "ALL"}
        bg="#F3F4F6"
        border="#E5E7EB"
        icon="inbox-multiple-outline"
        label="전체"
        textColor="#374151"
        onPress={() => onChange("ALL")}
      />
      <Pill
        active={value === "CREDITOR"}
        bg="#FFF4EB"
        border="#FFE3CF"
        icon="arrow-up-circle-outline"
        label="빌려준 것"
        textColor="#E86A17"
        onPress={() => onChange("CREDITOR")}
      />
      <Pill
        active={value === "DEBTOR"}
        bg="#EDF5FF"
        border="#D4E6FF"
        icon="arrow-down-circle-outline"
        label="빌린 것"
        textColor="#2E78F6"
        onPress={() => onChange("DEBTOR")}
      />
    </View>
  );
}

function Pill({
  active,
  bg,
  border,
  icon,
  label,
  textColor,
  onPress,
}: {
  active: boolean;
  bg: string;
  border: string;
  icon: any;
  label: string;
  textColor: string;
  onPress: () => void;
}) {
  return (
    <TouchableOpacity
      activeOpacity={0.9}
      onPress={onPress}
      style={[
        styles.pill,
        { backgroundColor: bg, borderColor: border },
        active && styles.pillActive,
      ]}
    >
      <MaterialCommunityIcons name={icon} size={16} color={textColor} />
      <Text style={[styles.pillText, { color: textColor }]} numberOfLines={1}>
        {label}
      </Text>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: "#fff" },
  searchbar: {
    backgroundColor: "#ffffffff",
    borderRadius: 0,
  },
  pillsRow: {
    flexDirection: "row",
    alignItems: "stretch",
    gap: 8, 
    marginHorizontal: 14,
    marginTop: 8,
    marginBottom: 8,
  },
  pill: {
    flex: 1, 
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    gap: 6,
    borderWidth: 1,
    borderRadius: 999,
    paddingHorizontal: 12,
    paddingVertical: 8,
    minHeight: 36,
  },
  pillText: {
    fontSize: 13,
    fontWeight: "700",
  },
  pillActive: {
    borderColor: "#111827",
    shadowColor: "#000",
    shadowOpacity: 0.06,
    shadowRadius: 6,
    shadowOffset: { width: 0, height: 3 },
    elevation: 2,
  },
});
export default MyAgreementsScreen;
