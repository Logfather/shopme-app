const {onDocumentWritten} = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

admin.initializeApp();

exports.updateItemCount = onDocumentWritten(
    "lists/{listId}/items/{itemId}",
    async (event) => {
      const listId = event.params.listId;

      const itemsRef = admin.firestore()
          .collection("lists")
          .doc(listId)
          .collection("items");

      const snapshot = await itemsRef
          .where("deletedAt", "==", null)
          .get();

      const count = snapshot.size;

      await admin.firestore()
        .collection("lists")
        .doc(listId)
        .update({
          itemCount: count,
          updatedAt: Date.now(), // 👈 HINZUFÜGEN
        });
    },
);
