import React from "react";
import {
  FlatList,
  SafeAreaView,
  StyleSheet,
  View
} from "react-native";
import { ActivityIndicator, Button, Text } from "react-native-paper";
import { AgreementHistoryCard } from "./components/AgreementHistoryCard";
import { ItemDetailsHeader } from "./components/ItemDetailsHeader";
import { useItemDetails } from "./hooks/useItemDetails";

const MyItemDetailScreen = ({ route, navigation }: any) => {
  const { itemId } = route.params;
  const {
    item,
    isLoading,
    agreementHistory,
    hasNext,
    isLoadingMore,
    fetchMoreData,
  } = useItemDetails(itemId, navigation);

  const renderListFooter = () => {
    if (isLoadingMore) {
      return (
        <View style={styles.listFooter}>
          <ActivityIndicator animating={true} size="small" />
        </View>
      );
    }

    if (!hasNext && agreementHistory.length > 0) {
      return (
        <View style={styles.listFooter}>
          <Text>마지막 기록입니다.</Text>
        </View>
      );
    }

    return null;
  };

  if (isLoading) {
    return (
      <View style={styles.centeredView}>
        <ActivityIndicator animating={true} size="large" />
      </View>
    );
  }

  if (!item) {
    return (
      <View style={styles.centeredView}>
        <Text>물품 정보를 찾을 수 없습니다.</Text>
        <Button mode="contained" onPress={() => navigation.goBack()}>
          뒤로가기
        </Button>
      </View>
    );
  }

  return (
    <SafeAreaView style={styles.container}>
      <FlatList
        data={agreementHistory}
        renderItem={({ item }) => <AgreementHistoryCard item={item} />}
        keyExtractor={(item) => item.id.toString()}
        onEndReached={fetchMoreData}
        onEndReachedThreshold={0.5}
        ListHeaderComponent={<ItemDetailsHeader item={item} />}
        ListFooterComponent={renderListFooter()}
      />
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#fff",
  },
  centeredView: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
  },
  listFooter: {
    padding: 20,
  },
});

export default MyItemDetailScreen;
