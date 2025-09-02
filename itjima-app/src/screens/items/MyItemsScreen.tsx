import React, { useCallback, useState } from "react";
import { getMyItemApi } from "../../api/items";
import { useFocusEffect } from "@react-navigation/native";
import { FlatList, RefreshControl, StyleSheet, View } from "react-native";
import { ActivityIndicator, Avatar, Card, FAB, Text } from "react-native-paper";

const MyItemsScreen = ({ navigation }: any) => {
  const [items, setItems] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);

  const fetchItems = async () => {
    try {
      const response = await getMyItemApi();
      setItems(response.data.data.items || []);
    } catch (error) {
      console.error("내 물품 목록을 불러오는데 실패했습니다:", error);
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  };

  useFocusEffect(
    useCallback(() => {
      setIsLoading(true);
      fetchItems();
    }, [])
  );
  const onRefresh = () => {
    setIsRefreshing(true);
    fetchItems();
  };

  if (isLoading) {
    return (
      <View style={styles.center}>
        <ActivityIndicator animating={true} size="large" />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      {items.length === 0 ? (
        <View style={styles.center}>
          <Text variant="titleMedium">등록된 물품이 없습니다.</Text>
          <Text variant="bodySmall">
            아래 버튼을 눌러 새 물품을 추가해보세요!
          </Text>
        </View>
      ) : (
        <FlatList
          data={items}
          keyExtractor={(item) => item.id.toString()}
          renderItem={({ item }) => (
            <Card>
              <Card.Title
                title={item.title}
                subtitle={item.description}
                left={(props) => (
                  <Avatar.Image
                    {...props}
                    source={{
                      uri: item.fileUrl || "https://via.placeholder.com/150",
                    }}
                  />
                )}
              />
              <Card.Content>
                <Text>{item.status}</Text>
              </Card.Content>
            </Card>
          )}
          refreshControl={
            <RefreshControl refreshing={isRefreshing} onRefresh={onRefresh} />
          }
          contentContainerStyle={{ padding: 8 }}
        />
      )}
      <FAB
        icon="plus"
        style={styles.fab}
        onPress={() => console.log("물품 추가 화면으로 이동")}
      />
    </View>
  );
};
const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  center: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
  },
  card: {
    margin: 8,
  },
  fab: {
    position: "absolute",
    margin: 16,
    right: 0,
    bottom: 0,
  },
});

export default MyItemsScreen;
