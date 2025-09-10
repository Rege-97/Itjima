import { IMG_BASE_URL } from "@env";
import { MaterialCommunityIcons } from "@expo/vector-icons";
import React from "react";
import { Image, StyleSheet, View } from "react-native";
import { Text } from "react-native-paper";
import { OverDue } from "../hooks/useHomeScreen";

function OverDueRow({ item }: { item: OverDue }) {
  const isMoney = item.amount != null;
  const tagStyle = isMoney ? styles.moneyTag : styles.itemTag;
  const tagText = isMoney ? "금전대여" : "물품대여";
  const due = (item.dueAt || "").slice(0, 10);

  return (
    <View
      style={[
        styles.rowBox,
        { borderLeftWidth: 3, borderLeftColor: "#ef4444" },
      ]}
    >
      {item.itemFileUrl ? (
        <Image
          source={{ uri: IMG_BASE_URL + item.itemFileUrl }}
          style={styles.thumb}
        />
      ) : (
        <View style={[styles.thumb, styles.thumbPlaceholder]}>
          <MaterialCommunityIcons
            name="image-off-outline"
            size={20}
            color="#9ca3af"
          />
        </View>
      )}

      <View style={{ flex: 1 }}>
        <View style={styles.rowTop}>
          <Text numberOfLines={1} style={styles.rowTitle}>
            {item.itemTitle}
          </Text>
          <View style={[styles.typeTag, tagStyle]}>
            <Text style={styles.typeTagText}>{tagText}</Text>
          </View>
        </View>

        {item.itemDescription ? (
          <Text numberOfLines={1} style={styles.rowDesc}>
            {item.itemDescription}
          </Text>
        ) : null}

        <View style={styles.rowMeta}>
          <View style={styles.metaChip}>
            <MaterialCommunityIcons
              name="calendar-alert"
              size={14}
              color="#ef4444"
            />
            <Text style={[styles.metaText, { color: "#ef4444" }]}>{due}</Text>
          </View>
          <View style={styles.metaChip}>
            <MaterialCommunityIcons
              name="alert-circle-outline"
              size={14}
              color="#ef4444"
            />
            <Text style={[styles.metaText, { color: "#ef4444" }]}>
              D+{item.overDays}
            </Text>
          </View>
          {isMoney && (
            <View style={styles.metaChip}>
              <MaterialCommunityIcons name="cash" size={14} color="#6b7280" />
              <Text style={styles.metaText}>
                {item.amount?.toLocaleString()}원
              </Text>
            </View>
          )}
        </View>
      </View>
    </View>
  );
}

export default OverDueRow;

const styles = StyleSheet.create({
  rowBox: {
    flexDirection: "row",
    gap: 12,
    padding: 12,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: "#E5E7EB",
    backgroundColor: "#FFFFFF",
  },
  thumb: {
    width: 56,
    height: 56,
    borderRadius: 8,
  },
  thumbPlaceholder: {
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#F3F4F6",
  },

  rowTop: {
    flexDirection: "row",
    alignItems: "center",
    gap: 8,
  },
  rowTitle: {
    flex: 1,
    fontWeight: "700",
  },
  rowDesc: {
    color: "#6b7280",
    marginTop: 2,
  },

  typeTag: {
    paddingHorizontal: 10,
    paddingVertical: 3,
    borderRadius: 6,
    borderWidth: 1,
  },
  moneyTag: {
    backgroundColor: "#FF9800",
  },
  itemTag: {
    backgroundColor: "#5c36f4",
  },
  typeTagText: {
    fontSize: 11,
    fontWeight: "700",
    color: "#ffffffff",
  },

  rowMeta: {
    flexDirection: "row",
    alignItems: "center",
    gap: 10,
    marginTop: 6,
    flexWrap: "wrap",
  },
  metaChip: {
    flexDirection: "row",
    alignItems: "center",
    gap: 4,
    paddingVertical: 2,
  },
  metaText: {
    color: "#6b7280",
    fontSize: 12,
  },
});
